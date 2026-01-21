package com.powermem.sdk.intelligence;

/**
 * Ebbinghaus forgetting curve algorithm implementation.
 *
 * <p>Python reference: {@code src/powermem/intelligence/ebbinghaus_algorithm.py}</p>
 */
public class EbbinghausAlgorithm {
    /**
     * Apply a simple forgetting curve decay to a base similarity score.
     *
     * <p>We use an exponential decay: {@code score = base * exp(-rate * hours)} where {@code rate} is
     * configurable via {@link com.powermem.sdk.config.IntelligentMemoryConfig#getDecayForgettingRate()}.</p>
     */
    public double apply(double baseScore,
                        java.time.Instant lastAccessedAt,
                        java.time.Instant now,
                        com.powermem.sdk.config.IntelligentMemoryConfig config) {
        if (config == null || !config.isEnabled() || !config.isDecayEnabled()) {
            return baseScore;
        }
        if (lastAccessedAt == null || now == null) {
            return baseScore;
        }
        long seconds = java.time.Duration.between(lastAccessedAt, now).getSeconds();
        if (seconds <= 0) {
            return baseScore;
        }
        double hours = seconds / 3600.0;
        double rate = config.getDecayForgettingRate();
        double baseRetention = config.getDecayBaseRetention();
        double factor = baseRetention * Math.exp(-rate * hours);
        return baseScore * factor;
    }
}

