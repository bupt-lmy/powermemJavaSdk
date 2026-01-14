package com.powermem.sdk.transport;

/**
 * JDK11 {@code java.net.http.HttpClient}-based transport implementation.
 *
 * <p>Optional infrastructure: used when the Java SDK needs to call external HTTP APIs (LLM/embedding),
 * or when providing a remote-client mode.</p>
 *
 * <p>No direct Python equivalent; conceptually similar to Python's {@code httpx} usage.</p>
 */
public class JavaHttpTransport implements HttpTransport {
}

