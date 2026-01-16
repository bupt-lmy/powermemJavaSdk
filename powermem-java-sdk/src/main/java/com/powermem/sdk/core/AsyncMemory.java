package com.powermem.sdk.core;

/**
 * Asynchronous PowerMem memory manager (pure Java core migration target).
 *
 * <p>Intended Java counterpart of Python {@code AsyncMemory}. In Java, async APIs typically expose
 * {@code CompletableFuture} or reactive types; the internal implementation may still share most of the
 * same orchestration code as {@link Memory}.</p>
 *
 * <p>Python reference: {@code src/powermem/core/async_memory.py}</p>
 */
public class AsyncMemory implements MemoryBase {
    @Override
    public com.powermem.sdk.model.AddMemoryResponse add(com.powermem.sdk.model.AddMemoryRequest request) {
        throw new UnsupportedOperationException("AsyncMemory.add is not implemented yet.");
    }

    @Override
    public com.powermem.sdk.model.SearchMemoriesResponse search(com.powermem.sdk.model.SearchMemoriesRequest request) {
        throw new UnsupportedOperationException("AsyncMemory.search is not implemented yet.");
    }

    @Override
    public com.powermem.sdk.model.UpdateMemoryResponse update(com.powermem.sdk.model.UpdateMemoryRequest request) {
        throw new UnsupportedOperationException("AsyncMemory.update is not implemented yet.");
    }

    @Override
    public com.powermem.sdk.model.DeleteMemoryResponse delete(String memoryId, String userId, String agentId) {
        throw new UnsupportedOperationException("AsyncMemory.delete is not implemented yet.");
    }

    @Override
    public com.powermem.sdk.model.GetAllMemoriesResponse getAll(com.powermem.sdk.model.GetAllMemoriesRequest request) {
        throw new UnsupportedOperationException("AsyncMemory.getAll is not implemented yet.");
    }

    @Override
    public com.powermem.sdk.model.DeleteAllMemoriesResponse deleteAll(
            com.powermem.sdk.model.DeleteAllMemoriesRequest request) {
        throw new UnsupportedOperationException("AsyncMemory.deleteAll is not implemented yet.");
    }
}

