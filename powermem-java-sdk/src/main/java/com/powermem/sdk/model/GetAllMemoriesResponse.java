package com.powermem.sdk.model;

/**
 * Response DTO for listing memories.
 *
 * <p>Python reference: return shape in {@code Memory.get_all} docstring in
 * {@code src/powermem/core/memory.py}.</p>
 */
public class GetAllMemoriesResponse {
    private java.util.List<MemoryRecord> memories = new java.util.ArrayList<>();

    public GetAllMemoriesResponse() {}

    public GetAllMemoriesResponse(java.util.List<MemoryRecord> memories) {
        if (memories != null) {
            this.memories = memories;
        }
    }

    public java.util.List<MemoryRecord> getMemories() {
        return memories;
    }

    public void setMemories(java.util.List<MemoryRecord> memories) {
        this.memories = memories == null ? new java.util.ArrayList<>() : memories;
    }
}

