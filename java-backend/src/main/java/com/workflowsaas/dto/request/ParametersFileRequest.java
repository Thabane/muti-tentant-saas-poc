package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for parameters file upload.
 */
public record ParametersFileRequest(
    @NotNull(message = "Filename is required")
    String filename,
    
    @NotNull(message = "Content is required")
    String content
) {}
