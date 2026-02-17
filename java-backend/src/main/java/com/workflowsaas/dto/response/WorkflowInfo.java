package com.workflowsaas.dto.response;

import java.util.UUID;

/**
 * Minimal workflow information for execution results.
 */
public record WorkflowInfo(
    UUID id,
    String name
) {}
