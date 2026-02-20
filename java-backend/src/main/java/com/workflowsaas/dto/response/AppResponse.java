package com.workflowsaas.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for App with nested workflows.
 */
public record AppResponse(
    UUID id,
    String name,
    String apiKey,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<WorkflowSummary> workflows
) {}
