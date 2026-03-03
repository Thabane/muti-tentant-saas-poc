package com.workflowsaas.dto.response;

import java.util.UUID;

/**
 * Response DTO for warehouse publisher configuration.
 */
public record WarehousePublisherResponse(
    UUID id,
    String avroSchema,
    String mappings
) {}
