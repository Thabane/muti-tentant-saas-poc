package com.workflowsaas.dto.response;

import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for publisher configuration.
 */
public record PublisherConfigResponse(
    UUID id,
    String type,
    String openApiDocument,
    String requestMappings,
    Map<String, String> configProperties,
    WarehousePublisherResponse warehousePublisher
) {}
