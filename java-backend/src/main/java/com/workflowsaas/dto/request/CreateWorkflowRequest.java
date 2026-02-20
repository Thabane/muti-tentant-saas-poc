package com.workflowsaas.dto.request;

import java.util.UUID;

/**
 * Request DTO for creating a workflow.
 */
public record CreateWorkflowRequest(
    String name,
    String type,
    String bpmnXml,
    String dmnXml,
    UUID appId,
    UUID parentWorkflowId,
    String subService
) {}
