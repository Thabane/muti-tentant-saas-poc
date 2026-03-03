package com.workflowsaas.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for app configuration.
 */
public record AppConfigResponse(
    UUID id,
    UUID appId,
    String source,
    List<EnrichmentApiConfigResponse> enrichmentApis,
    ParametersFileResponse parametersFile,
    ModelFileResponse modelFile,
    PublisherConfigResponse publisher,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
