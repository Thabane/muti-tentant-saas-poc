package com.workflowsaas.dto.request;

import java.util.Map;

/**
 * Request DTO for workflow test run.
 */
public record TestRunRequest(
    Map<String, Object> inputData
) {}
