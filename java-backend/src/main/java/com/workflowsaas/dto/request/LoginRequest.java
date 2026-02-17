package com.workflowsaas.dto.request;

/**
 * Request DTO for tenant login.
 */
public record LoginRequest(
    String email,
    String password
) {}
