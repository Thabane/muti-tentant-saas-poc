package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for model file upload.
 */
public record ModelFileRequest(
    @NotNull(message = "Filename is required")
    String filename,
    
    @NotNull(message = "Content is required")
    String content
) {}
