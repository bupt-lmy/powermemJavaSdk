package com.powermem.sdk.client;

/**
 * Convenience client focused on asynchronous memory CRUD operations.
 *
 * <p>In a pure Java core design this becomes a thin wrapper over
 * {@link com.powermem.sdk.core.AsyncMemory}.</p>
 *
 * <p>Python reference: {@code src/powermem/core/async_memory.py} (AsyncMemory)</p>
 */
public class AsyncMemoryClient {
    private final com.powermem.sdk.core.AsyncMemory memory;

    public AsyncMemoryClient(com.powermem.sdk.core.AsyncMemory memory) {
        this.memory = memory;
    }

    public com.powermem.sdk.model.AddMemoryResponse add(com.powermem.sdk.model.AddMemoryRequest request) {
        return memory.add(request);
    }

    public com.powermem.sdk.model.SearchMemoriesResponse search(com.powermem.sdk.model.SearchMemoriesRequest request) {
        return memory.search(request);
    }

    public com.powermem.sdk.model.UpdateMemoryResponse update(com.powermem.sdk.model.UpdateMemoryRequest request) {
        return memory.update(request);
    }

    public com.powermem.sdk.model.DeleteMemoryResponse delete(String memoryId, String userId, String agentId) {
        return memory.delete(memoryId, userId, agentId);
    }

    public com.powermem.sdk.model.GetAllMemoriesResponse getAll(com.powermem.sdk.model.GetAllMemoriesRequest request) {
        return memory.getAll(request);
    }

    public com.powermem.sdk.model.DeleteAllMemoriesResponse deleteAll(com.powermem.sdk.model.DeleteAllMemoriesRequest request) {
        return memory.deleteAll(request);
    }
}

