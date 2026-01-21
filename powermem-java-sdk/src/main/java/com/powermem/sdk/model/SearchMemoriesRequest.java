package com.powermem.sdk.model;

/**
 * Request DTO for semantic search.
 *
 * <p>Python reference: {@code Memory.search(...)} in {@code src/powermem/core/memory.py} and
 * REST request model {@code SearchRequest} in {@code benchmark/server/main.py}.</p>
 */
public class SearchMemoriesRequest {
    private String query;
    private String userId;
    private String agentId;
    private int topK = 5;

    public SearchMemoriesRequest() {}

    public static SearchMemoriesRequest ofQuery(String query, String userId) {
        SearchMemoriesRequest r = new SearchMemoriesRequest();
        r.setQuery(query);
        r.setUserId(userId);
        return r;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}

