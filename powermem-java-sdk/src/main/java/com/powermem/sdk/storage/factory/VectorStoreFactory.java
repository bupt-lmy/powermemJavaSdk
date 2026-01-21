package com.powermem.sdk.storage.factory;

/**
 * Factory for creating {@link com.powermem.sdk.storage.base.VectorStore} implementations based on
 * configuration.
 *
 * <p>Python reference: {@code src/powermem/storage/factory.py} (VectorStoreFactory)</p>
 */
public final class VectorStoreFactory {
    private VectorStoreFactory() {}

    public static com.powermem.sdk.storage.base.VectorStore fromConfig(com.powermem.sdk.config.VectorStoreConfig config) {
        String provider = config == null ? null : config.getProvider();
        if (provider == null || provider.isBlank() || "sqlite".equalsIgnoreCase(provider)) {
            String path = config == null ? null : config.getDatabasePath();
            boolean wal = config != null && config.isEnableWal();
            int timeout = config == null ? 30 : config.getTimeoutSeconds();
            return new com.powermem.sdk.storage.sqlite.SQLiteVectorStore(path, wal, timeout);
        }
        if ("oceanbase".equalsIgnoreCase(provider) || "ob".equalsIgnoreCase(provider)) {
            return new com.powermem.sdk.storage.oceanbase.OceanBaseVectorStore();
        }
        if ("pgvector".equalsIgnoreCase(provider) || "postgres".equalsIgnoreCase(provider) || "postgresql".equalsIgnoreCase(provider)) {
            return new com.powermem.sdk.storage.pgvector.PGVectorStore();
        }
        // Default to sqlite/in-memory for now.
        return new com.powermem.sdk.storage.sqlite.SQLiteVectorStore();
    }
}

