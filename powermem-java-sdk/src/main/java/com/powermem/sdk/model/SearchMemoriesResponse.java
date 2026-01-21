package com.powermem.sdk.model;

/**
 * Response DTO for semantic search.
 *
 * <p>Python reference: return shape in {@code Memory.search} docstring in
 * {@code src/powermem/core/memory.py}.</p>
 */
public class SearchMemoriesResponse {
    public static final class SearchResult {
        private MemoryRecord memory;
        private double score;

        public SearchResult() {}

        public SearchResult(MemoryRecord memory, double score) {
            this.memory = memory;
            this.score = score;
        }

        public MemoryRecord getMemory() {
            return memory;
        }

        public void setMemory(MemoryRecord memory) {
            this.memory = memory;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }

    private java.util.List<SearchResult> results = new java.util.ArrayList<>();

    public SearchMemoriesResponse() {}

    public SearchMemoriesResponse(java.util.List<SearchResult> results) {
        if (results != null) {
            this.results = results;
        }
    }

    public java.util.List<SearchResult> getResults() {
        return results;
    }

    public void setResults(java.util.List<SearchResult> results) {
        this.results = results == null ? new java.util.ArrayList<>() : results;
    }
}

