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
    private final Memory delegate;

    public AsyncMemory() {
        this(new com.powermem.sdk.config.MemoryConfig());
    }

    public AsyncMemory(com.powermem.sdk.config.MemoryConfig config) {
        this.delegate = new Memory(config);
    }

    @Override
    public com.powermem.sdk.model.AddMemoryResponse add(com.powermem.sdk.model.AddMemoryRequest request) {
        return delegate.add(request);
    }

    @Override
    public com.powermem.sdk.model.SearchMemoriesResponse search(com.powermem.sdk.model.SearchMemoriesRequest request) {
        return delegate.search(request);
    }

    @Override
    public com.powermem.sdk.model.UpdateMemoryResponse update(com.powermem.sdk.model.UpdateMemoryRequest request) {
        return delegate.update(request);
    }

    @Override
    public com.powermem.sdk.model.DeleteMemoryResponse delete(String memoryId, String userId, String agentId) {
        return delegate.delete(memoryId, userId, agentId);
    }

    @Override
    public com.powermem.sdk.model.GetAllMemoriesResponse getAll(com.powermem.sdk.model.GetAllMemoriesRequest request) {
        return delegate.getAll(request);
    }

    @Override
    public com.powermem.sdk.model.DeleteAllMemoriesResponse deleteAll(
            com.powermem.sdk.model.DeleteAllMemoriesRequest request) {
        return delegate.deleteAll(request);
    }
}

