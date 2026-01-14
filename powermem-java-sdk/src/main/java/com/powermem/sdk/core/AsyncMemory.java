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
}

