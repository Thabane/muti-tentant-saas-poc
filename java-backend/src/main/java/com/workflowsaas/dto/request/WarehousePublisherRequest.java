package com.workflowsaas.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for warehouse publisher configuration.
 */
public record WarehousePublisherRequest(
    @NotNull(message = "Avro schema is required")
    String avroSchema,
    
    String mappings
) {}
