package com.workflowsaas.dto.request;

/**
 * Request DTO for updating a workflow.
 */
public record UpdateWorkflowRequest(
    String name,
    String bpmnXml,
    String dmnXml
) {}
