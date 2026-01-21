package com.powermem.sdk.model;

/**
 * Canonical memory record as stored/retrieved from the underlying store.
 *
 * <p>Python reference: stored fields in {@code src/powermem/core/memory.py} (memory_data dict) and
 * storage adapters in {@code src/powermem/storage/adapter.py}.</p>
 */
public class MemoryRecord {
    private String id;
    private String content;
    private String userId;
    private String agentId;
    private String runId;
    private java.util.Map<String, Object> metadata;
    private java.time.Instant createdAt;
    private java.time.Instant updatedAt;
    private java.time.Instant lastAccessedAt;

    public MemoryRecord() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public java.util.Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(java.util.Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public java.time.Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(java.time.Instant lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
}

