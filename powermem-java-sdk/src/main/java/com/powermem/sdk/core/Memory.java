package com.powermem.sdk.core;

/**
 * Synchronous PowerMem memory manager (pure Java core migration target).
 *
 * <p>This class is intended to become the Java counterpart of the Python synchronous {@code Memory}
 * implementation, including orchestration of configuration, embedding, storage adapters, intelligent
 * memory, auditing, and telemetry.</p>
 *
 * <p>Python reference: {@code src/powermem/core/memory.py}</p>
 */
public class Memory implements MemoryBase {
    private final com.powermem.sdk.config.MemoryConfig config;
    private final com.powermem.sdk.storage.base.VectorStore vectorStore;
    private final com.powermem.sdk.integrations.embeddings.Embedder embedder;
    private final com.powermem.sdk.intelligence.IntelligenceManager intelligence;
    private final com.powermem.sdk.util.SnowflakeIdGenerator idGenerator;

    public Memory() {
        this(com.powermem.sdk.config.ConfigLoader.fromEnv());
    }

    public Memory(com.powermem.sdk.config.MemoryConfig config) {
        this.config = config == null ? new com.powermem.sdk.config.MemoryConfig() : config;
        this.vectorStore = com.powermem.sdk.storage.factory.VectorStoreFactory.fromConfig(this.config.getVectorStore());
        this.embedder = com.powermem.sdk.integrations.embeddings.EmbedderFactory.fromConfig(this.config.getEmbedder());
        this.intelligence = new com.powermem.sdk.intelligence.IntelligenceManager(this.config.getIntelligentMemory());
        this.idGenerator = com.powermem.sdk.util.SnowflakeIdGenerator.defaultGenerator();
    }

    @Override
    public com.powermem.sdk.model.AddMemoryResponse add(com.powermem.sdk.model.AddMemoryRequest request) {
        com.powermem.sdk.util.Preconditions.requireNonNull(request, "AddMemoryRequest is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getUserId(), "userId is required");

        String normalized = com.powermem.sdk.util.PowermemUtils.normalizeInput(request.getText(), request.getMessages());
        if (normalized.isBlank()) {
            return new com.powermem.sdk.model.AddMemoryResponse(java.util.Collections.emptyList());
        }

        java.time.Instant now = java.time.Instant.now();
        com.powermem.sdk.model.MemoryRecord record = new com.powermem.sdk.model.MemoryRecord();
        record.setId(idGenerator.nextId());
        record.setContent(normalized);
        record.setUserId(request.getUserId());
        record.setAgentId(request.getAgentId());
        record.setRunId(request.getRunId());
        record.setMetadata(request.getMetadata());
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setLastAccessedAt(now);

        float[] embedding = embedder.embed(normalized);
        vectorStore.upsert(record, embedding);

        java.util.List<com.powermem.sdk.model.MemoryRecord> saved = new java.util.ArrayList<>();
        saved.add(record);
        return new com.powermem.sdk.model.AddMemoryResponse(saved);
    }

    @Override
    public com.powermem.sdk.model.SearchMemoriesResponse search(com.powermem.sdk.model.SearchMemoriesRequest request) {
        com.powermem.sdk.util.Preconditions.requireNonNull(request, "SearchMemoriesRequest is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getUserId(), "userId is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getQuery(), "query is required");

        float[] queryVec = embedder.embed(request.getQuery());
        java.util.List<com.powermem.sdk.storage.base.OutputData> raw = vectorStore.search(
                queryVec, request.getTopK(), request.getUserId(), request.getAgentId());

        java.util.List<com.powermem.sdk.model.SearchMemoriesResponse.SearchResult> results = intelligence.postProcess(raw);
        return new com.powermem.sdk.model.SearchMemoriesResponse(results);
    }

    @Override
    public com.powermem.sdk.model.UpdateMemoryResponse update(com.powermem.sdk.model.UpdateMemoryRequest request) {
        com.powermem.sdk.util.Preconditions.requireNonNull(request, "UpdateMemoryRequest is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getUserId(), "userId is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getMemoryId(), "memoryId is required");

        // For the current in-memory store, we implement update by listing and matching id.
        java.util.List<com.powermem.sdk.model.MemoryRecord> all = vectorStore.list(
                request.getUserId(), request.getAgentId(), 0, Integer.MAX_VALUE);
        com.powermem.sdk.model.MemoryRecord target = null;
        for (com.powermem.sdk.model.MemoryRecord r : all) {
            if (r != null && request.getMemoryId().equals(r.getId())) {
                target = r;
                break;
            }
        }
        if (target == null) {
            return new com.powermem.sdk.model.UpdateMemoryResponse(null);
        }
        if (request.getNewContent() != null && !request.getNewContent().isBlank()) {
            target.setContent(request.getNewContent());
        }
        if (request.getMetadata() != null) {
            target.setMetadata(request.getMetadata());
        }
        java.time.Instant now = java.time.Instant.now();
        target.setUpdatedAt(now);
        float[] embedding = embedder.embed(target.getContent() == null ? "" : target.getContent());
        vectorStore.upsert(target, embedding);
        return new com.powermem.sdk.model.UpdateMemoryResponse(target);
    }

    @Override
    public com.powermem.sdk.model.DeleteMemoryResponse delete(String memoryId, String userId, String agentId) {
        com.powermem.sdk.util.Preconditions.requireNonBlank(userId, "userId is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(memoryId, "memoryId is required");
        boolean deleted = vectorStore.delete(memoryId, userId, agentId);
        return new com.powermem.sdk.model.DeleteMemoryResponse(deleted);
    }

    @Override
    public com.powermem.sdk.model.GetAllMemoriesResponse getAll(com.powermem.sdk.model.GetAllMemoriesRequest request) {
        com.powermem.sdk.util.Preconditions.requireNonNull(request, "GetAllMemoriesRequest is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getUserId(), "userId is required");
        java.util.List<com.powermem.sdk.model.MemoryRecord> list = vectorStore.list(
                request.getUserId(), request.getAgentId(), request.getOffset(), request.getLimit());
        return new com.powermem.sdk.model.GetAllMemoriesResponse(list);
    }

    @Override
    public com.powermem.sdk.model.DeleteAllMemoriesResponse deleteAll(
            com.powermem.sdk.model.DeleteAllMemoriesRequest request) {
        com.powermem.sdk.util.Preconditions.requireNonNull(request, "DeleteAllMemoriesRequest is required");
        com.powermem.sdk.util.Preconditions.requireNonBlank(request.getUserId(), "userId is required");
        int deleted = vectorStore.deleteAll(request.getUserId(), request.getAgentId());
        return new com.powermem.sdk.model.DeleteAllMemoriesResponse(deleted);
    }
}

