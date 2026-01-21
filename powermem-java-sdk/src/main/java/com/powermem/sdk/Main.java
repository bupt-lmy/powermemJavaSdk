package com.powermem.sdk;

/**
 * Minimal entry point for local/manual experiments with the Java SDK.
 *
 * <p>No direct Python equivalent. In Python, examples typically live under {@code examples/} and docs
 * notebooks.</p>
 */
public class Main {
    public static void main(String[] args) {
        com.powermem.sdk.config.MemoryConfig config = com.powermem.sdk.config.MemoryConfig.builder()
                .vectorStore(com.powermem.sdk.config.VectorStoreConfig.sqlite("./data/powermem_dev.db"))
                .build();

        com.powermem.sdk.core.Memory memory = new com.powermem.sdk.core.Memory(config);

        memory.add(com.powermem.sdk.model.AddMemoryRequest.ofText("用户喜欢简洁的中文回答", "user123"));
        memory.add(com.powermem.sdk.model.AddMemoryRequest.ofText("用户经常在周三晚上学习 Java", "user123"));

        com.powermem.sdk.model.SearchMemoriesResponse resp =
                memory.search(com.powermem.sdk.model.SearchMemoriesRequest.ofQuery("用户偏好是什么？", "user123"));

        System.out.println("Search results:");
        for (com.powermem.sdk.model.SearchMemoriesResponse.SearchResult r : resp.getResults()) {
            if (r == null || r.getMemory() == null) {
                continue;
            }
            System.out.println("- score=" + r.getScore() + " id=" + r.getMemory().getId()
                    + " content=" + r.getMemory().getContent());
        }
    }
}