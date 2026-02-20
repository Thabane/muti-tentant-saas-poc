package com.workflowsaas.service;

import java.util.UUID;

/**
 * Service for generating API paths for BPMN services.
 */
public interface ApiPathGenerator {
    
    /**
     * Generates API path following pattern: /{tenant-id}/{sub-service}/{service-name}/v{version}
     * - Converts service name to lowercase
     * - Replaces spaces with hyphens
     * - Ensures uniqueness within tenant/sub-service scope
     * 
     * @param tenantId the tenant ID
     * @param subService the sub-service name
     * @param serviceName the service name
     * @param version the version number
     * @return generated API path
     */
    String generateApiPath(UUID tenantId, String subService, String serviceName, Integer version);
    
    /**
     * Validates that the generated path is unique.
     * 
     * @param apiPath the API path to validate
     * @return true if unique, false otherwise
     */
    boolean isPathUnique(String apiPath);
}
