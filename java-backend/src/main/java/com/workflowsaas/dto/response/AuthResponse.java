package com.workflowsaas.dto.response;

/**
 * Response DTO for authentication (login/register).
 * Contains tenant profile and JWT token.
 */
public record AuthResponse(
    TenantResponse tenant,
    String token
) {}
