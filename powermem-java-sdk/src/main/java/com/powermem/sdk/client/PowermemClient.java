package com.powermem.sdk.client;

/**
 * High-level Java SDK facade.
 *
 * <p>In the pure-Java-core migration direction, this facade should primarily delegate to
 * {@link com.powermem.sdk.core.Memory}/{@link com.powermem.sdk.core.AsyncMemory} and provide a
 * convenient, Java-idiomatic entry point (builder/config, thread-safe reuse, etc.).</p>
 *
 * <p>Closest Python reference: {@code src/powermem/__init__.py} ({@code create_memory/from_config})</p>
 */
public class PowermemClient {
    private final com.powermem.sdk.config.MemoryConfig config;
    private final com.powermem.sdk.core.Memory memory;
    private final com.powermem.sdk.core.AsyncMemory asyncMemory;

    public PowermemClient(com.powermem.sdk.config.MemoryConfig config) {
        this.config = config == null ? new com.powermem.sdk.config.MemoryConfig() : config;
        this.memory = new com.powermem.sdk.core.Memory(this.config);
        this.asyncMemory = new com.powermem.sdk.core.AsyncMemory(this.config);
    }

    public static PowermemClientBuilder builder() {
        return PowermemClientBuilder.builder();
    }

    public com.powermem.sdk.config.MemoryConfig getConfig() {
        return config;
    }

    public com.powermem.sdk.core.Memory memory() {
        return memory;
    }

    public com.powermem.sdk.core.AsyncMemory asyncMemory() {
        return asyncMemory;
    }
}

