package com.workflowsaas.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Summary DTO for workflow with hierarchical children.
 */
public record WorkflowSummary(
    UUID id,
    String name,
    String type,
    String apiPath,
    List<WorkflowSummary> children
) {
    public WorkflowSummary(UUID id, String name, String type, String apiPath) {
        this(id, name, type, apiPath, new ArrayList<>());
    }
}
