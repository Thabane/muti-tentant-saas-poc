package com.workflowsaas.dto.response;

/**
 * Response DTO for error responses.
 * Stack trace is only included in development mode.
 */
public record ErrorResponse(
    String error,
    String stack
) {}
