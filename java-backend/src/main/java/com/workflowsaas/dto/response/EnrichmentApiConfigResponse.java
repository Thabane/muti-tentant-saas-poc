package com.workflowsaas.dto.response;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for enrichment API configuration.
 */
public record EnrichmentApiConfigResponse(
    UUID id,
    String openApiDocument,
    String requestMappings,
    Map<String, String> configProperties
) {}
