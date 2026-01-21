package com.powermem.sdk.storage.sqlite;

import com.powermem.sdk.storage.base.VectorStore;
import com.powermem.sdk.util.VectorMath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SQLite vector store implementation (Java migration target).
 *
 * <p>Python reference: {@code src/powermem/storage/sqlite/sqlite_vector_store.py} (SQLiteVectorStore)</p>
 */
public class SQLiteVectorStore implements VectorStore {
    private static final String TABLE_MEMORIES = "memories";
    private static final String TABLE_HISTORY = "history";

    private final String databasePath;
    private final boolean enableWal;
    private final int busyTimeoutSeconds;
    private final com.powermem.sdk.util.SnowflakeIdGenerator historyIdGenerator = com.powermem.sdk.util.SnowflakeIdGenerator.defaultGenerator();

    public SQLiteVectorStore() {
        this("./data/powermem_dev.db", true, 30);
    }

    public SQLiteVectorStore(String databasePath, boolean enableWal, int busyTimeoutSeconds) {
        this.databasePath = (databasePath == null || databasePath.isBlank()) ? "./data/powermem_dev.db" : databasePath;
        this.enableWal = enableWal;
        this.busyTimeoutSeconds = busyTimeoutSeconds <= 0 ? 30 : busyTimeoutSeconds;
        ensureInitialized();
    }

    private void ensureInitialized() {
        try {
            Path p = Paths.get(databasePath);
            Path parent = p.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (Exception ignored) {
            // best-effort
        }

        try (Connection c = openConnection(); Statement st = c.createStatement()) {
            if (enableWal) {
                st.execute("PRAGMA journal_mode=WAL;");
            }
            st.execute("PRAGMA foreign_keys=ON;");
            st.execute("PRAGMA busy_timeout=" + (busyTimeoutSeconds * 1000) + ";");

            st.execute("CREATE TABLE IF NOT EXISTS " + TABLE_MEMORIES + " ("
                    + "id TEXT PRIMARY KEY,"
                    + "user_id TEXT,"
                    + "agent_id TEXT,"
                    + "run_id TEXT,"
                    + "content TEXT NOT NULL,"
                    + "embedding TEXT,"
                    + "metadata TEXT,"
                    + "created_at INTEGER,"
                    + "updated_at INTEGER,"
                    + "last_accessed_at INTEGER"
                    + ");");
            st.execute("CREATE INDEX IF NOT EXISTS idx_memories_user_agent ON " + TABLE_MEMORIES + " (user_id, agent_id);");

            st.execute("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
                    + "id TEXT PRIMARY KEY,"
                    + "memory_id TEXT,"
                    + "old_memory TEXT,"
                    + "new_memory TEXT,"
                    + "event TEXT,"
                    + "created_at INTEGER,"
                    + "updated_at INTEGER,"
                    + "is_deleted INTEGER,"
                    + "actor_id TEXT,"
                    + "role TEXT"
                    + ");");
            st.execute("CREATE INDEX IF NOT EXISTS idx_history_memory_id ON " + TABLE_HISTORY + " (memory_id);");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize SQLite schema: " + ex.getMessage(), ex);
        }
    }

    private Connection openConnection() throws Exception {
        // Ensure driver is loaded when running in environments that don't use ServiceLoader.
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ignored) {
            // If dependency isn't on classpath, DriverManager.getConnection will fail with a clearer message below.
        }
        // sqlite-jdbc supports "jdbc:sqlite:/abs/path" and relative paths
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }

    @Override
    public void upsert(com.powermem.sdk.model.MemoryRecord record, float[] embedding) {
        if (record == null || record.getId() == null) {
            return;
        }
        String oldContent = null;
        boolean existed = false;
        try {
            oldContent = getContentById(record.getId());
            existed = oldContent != null;
        } catch (Exception ignored) {
            // best-effort
        }
        Instant now = Instant.now();
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        record.setUpdatedAt(now);
        if (record.getLastAccessedAt() == null) {
            record.setLastAccessedAt(now);
        }

        String embeddingStr = encodeEmbedding(embedding);
        String metadataStr = encodeMetadata(record.getMetadata());

        String sql = "INSERT INTO " + TABLE_MEMORIES
                + " (id, user_id, agent_id, run_id, content, embedding, metadata, created_at, updated_at, last_accessed_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                + " ON CONFLICT(id) DO UPDATE SET "
                + "user_id=excluded.user_id, agent_id=excluded.agent_id, run_id=excluded.run_id,"
                + "content=excluded.content, embedding=excluded.embedding, metadata=excluded.metadata,"
                + "updated_at=excluded.updated_at, last_accessed_at=excluded.last_accessed_at";
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, record.getId());
            ps.setString(2, record.getUserId());
            ps.setString(3, record.getAgentId());
            ps.setString(4, record.getRunId());
            ps.setString(5, record.getContent());
            ps.setString(6, embeddingStr);
            ps.setString(7, metadataStr);
            ps.setLong(8, toEpochMilli(record.getCreatedAt()));
            ps.setLong(9, toEpochMilli(record.getUpdatedAt()));
            ps.setLong(10, toEpochMilli(record.getLastAccessedAt()));
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new RuntimeException("SQLite upsert failed: " + ex.getMessage(), ex);
        }

        // best-effort audit
        String event = existed ? "UPDATE" : "ADD";
        writeHistory(record.getId(), oldContent, record.getContent(), event, record.getUserId(), record.getAgentId(), false);
    }

    @Override
    public boolean delete(String memoryId, String userId, String agentId) {
        if (memoryId == null) {
            return false;
        }
        String oldContent = null;
        try {
            oldContent = getContentById(memoryId);
        } catch (Exception ignored) {
            // best-effort
        }
        StringBuilder sb = new StringBuilder("DELETE FROM " + TABLE_MEMORIES + " WHERE id=?");
        List<Object> args = new ArrayList<>();
        args.add(memoryId);
        if (userId != null && !userId.isBlank()) {
            sb.append(" AND user_id=?");
            args.add(userId);
        }
        if (agentId != null && !agentId.isBlank()) {
            sb.append(" AND agent_id=?");
            args.add(agentId);
        }
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            int changed = ps.executeUpdate();
            boolean deleted = changed > 0;
            if (deleted) {
                writeHistory(memoryId, oldContent, null, "DELETE", userId, agentId, true);
            }
            return deleted;
        } catch (Exception ex) {
            throw new RuntimeException("SQLite delete failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public int deleteAll(String userId, String agentId) {
        // best-effort: snapshot ids for audit before deletion
        List<String> ids = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        try {
            StringBuilder q = new StringBuilder("SELECT id, content FROM " + TABLE_MEMORIES + " WHERE 1=1");
            List<Object> qArgs = new ArrayList<>();
            if (userId != null && !userId.isBlank()) {
                q.append(" AND user_id=?");
                qArgs.add(userId);
            }
            if (agentId != null && !agentId.isBlank()) {
                q.append(" AND agent_id=?");
                qArgs.add(agentId);
            }
            try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(q.toString())) {
                for (int i = 0; i < qArgs.size(); i++) {
                    ps.setObject(i + 1, qArgs.get(i));
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ids.add(rs.getString("id"));
                        contents.add(rs.getString("content"));
                    }
                }
            }
        } catch (Exception ignored) {
            // best-effort
        }

        StringBuilder sb = new StringBuilder("DELETE FROM " + TABLE_MEMORIES + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            sb.append(" AND user_id=?");
            args.add(userId);
        }
        if (agentId != null && !agentId.isBlank()) {
            sb.append(" AND agent_id=?");
            args.add(agentId);
        }
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            int deleted = ps.executeUpdate();
            // best-effort audit
            for (int i = 0; i < ids.size(); i++) {
                writeHistory(ids.get(i), contents.get(i), null, "DELETE", userId, agentId, true);
            }
            return deleted;
        } catch (Exception ex) {
            throw new RuntimeException("SQLite deleteAll failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<com.powermem.sdk.model.MemoryRecord> list(String userId, String agentId, int offset, int limit) {
        int safeOffset = Math.max(0, offset);
        int safeLimit = limit <= 0 ? 100 : limit;

        StringBuilder sb = new StringBuilder("SELECT id,user_id,agent_id,run_id,content,metadata,created_at,updated_at,last_accessed_at "
                + "FROM " + TABLE_MEMORIES + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            sb.append(" AND user_id=?");
            args.add(userId);
        }
        if (agentId != null && !agentId.isBlank()) {
            sb.append(" AND agent_id=?");
            args.add(agentId);
        }
        sb.append(" ORDER BY updated_at DESC LIMIT ? OFFSET ?");
        args.add(safeLimit);
        args.add(safeOffset);

        List<com.powermem.sdk.model.MemoryRecord> results = new ArrayList<>();
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(readRecord(rs));
                }
            }
            return results;
        } catch (Exception ex) {
            throw new RuntimeException("SQLite list failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<com.powermem.sdk.storage.base.OutputData> search(
            float[] queryEmbedding, int topK, String userId, String agentId) {
        int k = topK <= 0 ? 5 : topK;
        Instant now = Instant.now();

        StringBuilder sb = new StringBuilder("SELECT id,user_id,agent_id,run_id,content,embedding,metadata,created_at,updated_at,last_accessed_at "
                + "FROM " + TABLE_MEMORIES + " WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            sb.append(" AND user_id=?");
            args.add(userId);
        }
        if (agentId != null && !agentId.isBlank()) {
            sb.append(" AND agent_id=?");
            args.add(agentId);
        }

        List<com.powermem.sdk.storage.base.OutputData> scored = new ArrayList<>();
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.powermem.sdk.model.MemoryRecord record = readRecord(rs);
                    float[] emb = decodeEmbedding(rs.getString("embedding"));
                    double score = VectorMath.cosineSimilarity(queryEmbedding, emb);
                    record.setLastAccessedAt(now);
                    scored.add(new com.powermem.sdk.storage.base.OutputData(record, score));
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("SQLite search failed: " + ex.getMessage(), ex);
        }

        scored.sort(Comparator.comparingDouble(com.powermem.sdk.storage.base.OutputData::getScore).reversed());
        if (scored.size() > k) {
            scored = new ArrayList<>(scored.subList(0, k));
        }

        // best-effort: persist last_accessed_at for returned results
        for (com.powermem.sdk.storage.base.OutputData d : scored) {
            if (d == null || d.getRecord() == null) {
                continue;
            }
            updateLastAccessedAt(d.getRecord().getId(), now);
        }
        return scored;
    }

    private void updateLastAccessedAt(String id, Instant at) {
        if (id == null) {
            return;
        }
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE " + TABLE_MEMORIES + " SET last_accessed_at=? WHERE id=?")) {
            ps.setLong(1, toEpochMilli(at));
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private String getContentById(String id) throws Exception {
        if (id == null) {
            return null;
        }
        try (Connection c = openConnection();
             PreparedStatement ps = c.prepareStatement("SELECT content FROM " + TABLE_MEMORIES + " WHERE id=?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
            }
        }
        return null;
    }

    private void writeHistory(String memoryId,
                              String oldMemory,
                              String newMemory,
                              String event,
                              String userId,
                              String agentId,
                              boolean isDeleted) {
        if (memoryId == null || event == null) {
            return;
        }
        String sql = "INSERT INTO " + TABLE_HISTORY
                + " (id, memory_id, old_memory, new_memory, event, created_at, updated_at, is_deleted, actor_id, role)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Instant now = Instant.now();
        try (Connection c = openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, historyIdGenerator.nextId());
            ps.setString(2, memoryId);
            ps.setString(3, oldMemory);
            ps.setString(4, newMemory);
            ps.setString(5, event);
            ps.setLong(6, toEpochMilli(now));
            ps.setLong(7, toEpochMilli(now));
            ps.setInt(8, isDeleted ? 1 : 0);
            // best-effort actor
            ps.setString(9, agentId != null && !agentId.isBlank() ? agentId : userId);
            ps.setString(10, "sdk");
            ps.executeUpdate();
        } catch (Exception ignored) {
            // best-effort: auditing must not break main flow
        }
    }

    private static long toEpochMilli(Instant t) {
        return t == null ? 0L : t.toEpochMilli();
    }

    private static Instant fromEpochMilli(long v) {
        return v <= 0 ? null : Instant.ofEpochMilli(v);
    }

    private static com.powermem.sdk.model.MemoryRecord readRecord(ResultSet rs) throws Exception {
        com.powermem.sdk.model.MemoryRecord r = new com.powermem.sdk.model.MemoryRecord();
        r.setId(rs.getString("id"));
        r.setUserId(rs.getString("user_id"));
        r.setAgentId(rs.getString("agent_id"));
        r.setRunId(rs.getString("run_id"));
        r.setContent(rs.getString("content"));
        r.setMetadata(decodeMetadata(rs.getString("metadata")));
        r.setCreatedAt(fromEpochMilli(rs.getLong("created_at")));
        r.setUpdatedAt(fromEpochMilli(rs.getLong("updated_at")));
        r.setLastAccessedAt(fromEpochMilli(rs.getLong("last_accessed_at")));
        return r;
    }

    private static String encodeEmbedding(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(Float.toString(embedding[i]));
        }
        return sb.toString();
    }

    private static float[] decodeEmbedding(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String[] parts = s.split(",");
        float[] out = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                out[i] = Float.parseFloat(parts[i].trim());
            } catch (Exception ex) {
                out[i] = 0.0f;
            }
        }
        return out;
    }

    private static String encodeMetadata(java.util.Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        // Minimal encoding to keep dependency-free. Format: key=value;key2=value2
        StringBuilder sb = new StringBuilder();
        for (java.util.Map.Entry<String, Object> e : metadata.entrySet()) {
            if (e.getKey() == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(escape(e.getKey())).append('=').append(escape(String.valueOf(e.getValue())));
        }
        return sb.toString();
    }

    private static java.util.Map<String, Object> decodeMetadata(String s) {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        if (s == null || s.isBlank()) {
            return m;
        }
        String[] pairs = s.split(";");
        for (String p : pairs) {
            if (p == null || p.isBlank()) {
                continue;
            }
            int idx = p.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String k = unescape(p.substring(0, idx));
            String v = unescape(p.substring(idx + 1));
            m.put(k, v);
        }
        return m;
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace(";", "\\;").replace("=", "\\=");
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        boolean esc = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) {
                out.append(c);
                esc = false;
            } else if (c == '\\') {
                esc = true;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}

