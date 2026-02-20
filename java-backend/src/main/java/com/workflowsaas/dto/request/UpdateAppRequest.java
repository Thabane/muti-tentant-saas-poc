package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating an app.
 */
public record UpdateAppRequest(
    @NotBlank(message = "App name is required")
    String name
) {}
