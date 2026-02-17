package com.workflowsaas.dto.request;

/**
 * Request DTO for tenant registration.
 */
public record RegisterRequest(
    String name,
    String slug,
    String email,
    String password
) {}
