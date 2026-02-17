package com.workflowsaas.dto.request;

/**
 * Request DTO for promoting a deployment.
 */
public record PromoteRequest(
    String targetEnvironment,
    Integer rolloutPercentage
) {}
