package com.powermem.sdk.integrations.embeddings;

/**
 * Qwen/DashScope embedding implementation (Java migration target).
 *
 * <p>Python reference: {@code src/powermem/integrations/embeddings/qwen.py}</p>
 */
public class QwenEmbedder implements Embedder {
    @Override
    public float[] embed(String text) {
        throw new UnsupportedOperationException("QwenEmbedder not implemented yet.");
    }

    @Override
    public java.util.List<float[]> embedBatch(java.util.List<String> texts) {
        throw new UnsupportedOperationException("QwenEmbedder not implemented yet.");
    }
}

