package com.powermem.sdk.util;

/**
 * Shared internal utilities for the Java core migration (hashing, time helpers, message parsing, etc.).
 *
 * <p>This file is expected to host equivalents of commonly used helpers from the Python codebase.</p>
 *
 * <p>Python reference: {@code src/powermem/utils/utils.py}</p>
 */
public final class PowermemUtils {
    private PowermemUtils() {}

    /**
     * Normalize either raw text or message list into a single string for embedding/storage.
     */
    public static String normalizeInput(String text, java.util.List<com.powermem.sdk.model.Message> messages) {
        if (text != null && !text.isBlank()) {
            return text;
        }
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (com.powermem.sdk.model.Message m : messages) {
            if (m == null) {
                continue;
            }
            String role = m.getRole() == null ? "" : m.getRole().trim();
            String content = m.getContent() == null ? "" : m.getContent().trim();
            if (content.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            if (!role.isEmpty()) {
                sb.append(role).append(": ");
            }
            sb.append(content);
        }
        return sb.toString();
    }
}

