package com.powermem.sdk.integrations.embeddings;

import java.util.List;

/**
 * Embedding interface used by the PowerMem core to generate vector representations.
 *
 * <p>Python reference: {@code src/powermem/integrations/embeddings/base.py}</p>
 */
public interface Embedder {

    /**
     * Embed a single text input into a vector.
     *
     * @param text input text
     * @return embedding vector
     */
    float[] embed(String text);

    /**
     * Embed multiple text inputs into vectors.
     *
     * @param texts input texts
     * @return list of embedding vectors aligned with input order
     */
    List<float[]> embedBatch(List<String> texts);
}
