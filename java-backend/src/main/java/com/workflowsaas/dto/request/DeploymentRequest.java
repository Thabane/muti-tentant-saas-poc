package com.workflowsaas.dto.request;

import java.util.UUID;

/**
 * Request DTO for creating a deployment.
 */
public record DeploymentRequest(
    UUID workflowId,
    String environment,
    Integer rolloutPercentage
) {}
