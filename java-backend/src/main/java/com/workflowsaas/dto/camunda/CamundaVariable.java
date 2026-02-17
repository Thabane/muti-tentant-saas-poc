package com.workflowsaas.dto.camunda;

/**
 * DTO for Camunda typed variable.
 */
public record CamundaVariable(
    Object value,
    String type
) {}
