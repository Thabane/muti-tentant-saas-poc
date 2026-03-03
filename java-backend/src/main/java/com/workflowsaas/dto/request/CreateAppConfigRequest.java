package com.workflowsaas.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * Request DTO for creating or updating app configuration.
 */
public record CreateAppConfigRequest(
    @NotNull(message = "Source is required")
    @Pattern(regexp = "^(eeh|api)$", message = "Source must be 'eeh' or 'api'")
    String source,
    
    List<EnrichmentApiConfigRequest> enrichmentApis,
    
    @Valid
    ParametersFileRequest parametersFile,
    
    @Valid
    ModelFileRequest modelFile,
    
    @Valid
    PublisherConfigRequest publisher
) {}
