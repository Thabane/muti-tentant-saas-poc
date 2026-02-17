package com.workflowsaas.dto.request;

import java.util.Map;

/**
 * Request DTO for executing a workflow.
 */
public record ExecuteRequest(
    Map<String, Object> inputData
) {}
