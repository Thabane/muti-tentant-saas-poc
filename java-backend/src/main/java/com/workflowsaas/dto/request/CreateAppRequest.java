package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new app.
 */
public record CreateAppRequest(
    @NotBlank(message = "App name is required")
    String name
) {}
