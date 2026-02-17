package com.workflowsaas.dto.response;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for workflow execution result.
 */
public record WorkflowExecutionResult(
    UUID executionId,
    String status,
    Map<String, Object> input,
    Map<String, Object> output,
    WorkflowInfo workflow
) {}
