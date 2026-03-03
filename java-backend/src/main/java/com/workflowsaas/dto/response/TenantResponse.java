package com.workflowsaas.dto.response;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for tenant profile.
 * Excludes sensitive data like password hash.
 */
public record TenantResponse(
    UUID id,
    String name,
    String slug,
    String email,
    Map<String, Object> features,
    Map<String, Object> configuration,
    Boolean onboardingCompleted
) {}
