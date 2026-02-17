package com.workflowsaas.dto.request;

/**
 * Request DTO for creating a workflow.
 */
public record CreateWorkflowRequest(
    String name,
    String type,
    String bpmnXml,
    String dmnXml
) {}
