package com.powermem.sdk.exception;

/**
 * Exception representing API-level failures (validation, business rules, etc.).
 *
 * <p>No direct Python equivalent class; Python throws various exceptions depending on the layer.</p>
 */
public class ApiException extends PowermemException {
    public ApiException() {
        super();
    }
}

