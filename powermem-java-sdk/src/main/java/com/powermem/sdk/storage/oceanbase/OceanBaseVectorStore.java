package com.powermem.sdk.storage.oceanbase;

import com.powermem.sdk.storage.base.VectorStore;

/**
 * OceanBase vector store implementation (Java migration target).
 *
 * <p>Python reference: {@code src/powermem/storage/oceanbase/oceanbase.py} (OceanBaseVectorStore)</p>
 */
public class OceanBaseVectorStore implements VectorStore {
    @Override
    public void upsert(com.powermem.sdk.model.MemoryRecord record, float[] embedding) {
        throw new UnsupportedOperationException("OceanBaseVectorStore is not implemented yet.");
    }

    @Override
    public boolean delete(String memoryId, String userId, String agentId) {
        throw new UnsupportedOperationException("OceanBaseVectorStore is not implemented yet.");
    }

    @Override
    public int deleteAll(String userId, String agentId) {
        throw new UnsupportedOperationException("OceanBaseVectorStore is not implemented yet.");
    }

    @Override
    public java.util.List<com.powermem.sdk.model.MemoryRecord> list(String userId, String agentId, int offset, int limit) {
        throw new UnsupportedOperationException("OceanBaseVectorStore is not implemented yet.");
    }

    @Override
    public java.util.List<com.powermem.sdk.storage.base.OutputData> search(
            float[] queryEmbedding, int topK, String userId, String agentId) {
        throw new UnsupportedOperationException("OceanBaseVectorStore is not implemented yet.");
    }
}

