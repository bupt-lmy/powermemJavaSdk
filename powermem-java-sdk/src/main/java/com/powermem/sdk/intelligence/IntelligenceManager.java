package com.powermem.sdk.intelligence;

/**
 * Facade/manager for intelligent memory features (pure Java core migration target).
 *
 * <p>Python reference: {@code src/powermem/intelligence/manager.py}</p>
 */
public class IntelligenceManager {
    private final com.powermem.sdk.config.IntelligentMemoryConfig config;
    private final EbbinghausAlgorithm ebbinghaus = new EbbinghausAlgorithm();

    public IntelligenceManager(com.powermem.sdk.config.IntelligentMemoryConfig config) {
        this.config = config;
    }

    public boolean isEnabled() {
        return config != null && config.isEnabled() && config.isDecayEnabled();
    }

    public java.util.List<com.powermem.sdk.model.SearchMemoriesResponse.SearchResult> postProcess(
            java.util.List<com.powermem.sdk.storage.base.OutputData> raw) {
        java.util.List<com.powermem.sdk.model.SearchMemoriesResponse.SearchResult> results = new java.util.ArrayList<>();
        if (raw == null) {
            return results;
        }
        java.time.Instant now = java.time.Instant.now();
        for (com.powermem.sdk.storage.base.OutputData d : raw) {
            if (d == null) {
                continue;
            }
            com.powermem.sdk.model.MemoryRecord r = d.getRecord();
            double score = d.getScore();
            if (isEnabled() && r != null) {
                score = ebbinghaus.apply(score, r.getLastAccessedAt(), now, config);
            }
            results.add(new com.powermem.sdk.model.SearchMemoriesResponse.SearchResult(r, score));
        }
        results.sort(java.util.Comparator.comparingDouble(com.powermem.sdk.model.SearchMemoriesResponse.SearchResult::getScore).reversed());
        return results;
    }
}

