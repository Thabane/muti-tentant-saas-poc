package com.workflowsaas.dto.camunda;

/**
 * DTO for Camunda process instance.
 */
public record ProcessInstance(
    String id,
    String definitionId,
    String businessKey,
    String tenantId,
    boolean ended,
    boolean suspended
) {}
