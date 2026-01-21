package com.powermem.sdk.model;

/**
 * Response DTO for add memory operations.
 *
 * <p>Python reference: return shape described in {@code Memory.add} docstring in
 * {@code src/powermem/core/memory.py}.</p>
 */
public class AddMemoryResponse {
    private java.util.List<MemoryRecord> memories = new java.util.ArrayList<>();

    public AddMemoryResponse() {}

    public AddMemoryResponse(java.util.List<MemoryRecord> memories) {
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

