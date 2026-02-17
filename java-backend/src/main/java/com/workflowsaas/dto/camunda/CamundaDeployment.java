package com.workflowsaas.dto.camunda;

/**
 * DTO for Camunda deployment response.
 */
public record CamundaDeployment(
    String id,
    String name,
    String deploymentTime,
    String tenantId
) {}
