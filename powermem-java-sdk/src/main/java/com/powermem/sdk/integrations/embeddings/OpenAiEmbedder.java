package com.powermem.sdk.integrations.embeddings;

/**
 * OpenAI-compatible embedding implementation (Java migration target).
 *
 * <p>Python reference: {@code src/powermem/integrations/embeddings/openai.py}</p>
 */
public class OpenAiEmbedder implements Embedder {
    @Override
    public float[] embed(String text) {
        throw new UnsupportedOperationException("OpenAiEmbedder not implemented yet.");
    }

    @Override
    public java.util.List<float[]> embedBatch(java.util.List<String> texts) {
        throw new UnsupportedOperationException("OpenAiEmbedder not implemented yet.");
    }
}

