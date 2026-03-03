package com.workflowsaas.dto.response;

import java.util.Map;

/**
 * Response DTO for error responses.
 * Stack trace is only included in development mode.
 * Field-specific errors are included when validation fails.
 */
public record ErrorResponse(
    String error,
    Map<String, String> errors,
    String stack
) {
    public ErrorResponse(String error, String stack) {
        this(error, null, stack);
    }
    
    public ErrorResponse(String error, Map<String, String> errors) {
        this(error, errors, null);
    }
}
