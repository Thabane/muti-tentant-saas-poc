package com.workflowsaas.dto.response;

/**
 * Response DTO for parameters file.
 */
public record ParametersFileResponse(
    String filename,
    String content
) {}
