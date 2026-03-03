package com.workflowsaas.dto.response;

/**
 * Response DTO for model file.
 */
public record ModelFileResponse(
    String filename,
    String content
) {}
