package com.workflowsaas.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

/**
 * Request DTO for publisher configuration.
 */
public record PublisherConfigRequest(
    @NotNull(message = "Publisher type is required")
    @Pattern(regexp = "^(api|eeh)$", message = "Publisher type must be 'api' or 'eeh'")
    String type,
    
    String openApiDocument,
    
    String requestMappings,
    
    Map<String, String> configProperties,
    
    @Valid
    WarehousePublisherRequest warehousePublisher
) {}
