package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for enrichment API configuration.
 */
public record EnrichmentApiConfigRequest(
    @NotNull(message = "OpenAPI document is required")
    String openApiDocument,
    
    String requestMappings,
    
    Map<String, String> configProperties
) {}
