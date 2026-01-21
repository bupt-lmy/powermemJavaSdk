package com.powermem.sdk.storage.base;

/**
 * Normalized output record returned by vector store search/get/list operations.
 *
 * <p>Python reference: {@code src/powermem/storage/base.py} (OutputData)</p>
 */
public class OutputData {
    private com.powermem.sdk.model.MemoryRecord record;
    private double score;

    public OutputData() {}

    public OutputData(com.powermem.sdk.model.MemoryRecord record, double score) {
        this.record = record;
        this.score = score;
    }

    public com.powermem.sdk.model.MemoryRecord getRecord() {
        return record;
    }

    public void setRecord(com.powermem.sdk.model.MemoryRecord record) {
        this.record = record;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}

