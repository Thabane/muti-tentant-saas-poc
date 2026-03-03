package com.workflowsaas.service;

import com.workflowsaas.dto.request.CreateAppConfigRequest;
import com.workflowsaas.dto.response.AppConfigResponse;

import java.util.UUID;

/**
 * Service for managing app configurations.
 */
public interface AppConfigService {
    
    /**
     * Gets the configuration for an app.
     * 
     * @param appId the app ID
     * @return the app configuration
     */
    AppConfigResponse getConfig(UUID appId);
    
    /**
     * Creates a new configuration for an app.
     * 
     * @param appId the app ID
     * @param request the create configuration request
     * @return created configuration
     */
    AppConfigResponse createConfig(UUID appId, CreateAppConfigRequest request);
    
    /**
     * Updates the configuration for an app.
     * 
     * @param appId the app ID
     * @param request the update configuration request
     * @return updated configuration
     */
    AppConfigResponse updateConfig(UUID appId, CreateAppConfigRequest request);
    
    /**
     * Deletes an enrichment API from an app configuration.
     * 
     * @param appId the app ID
     * @param enrichmentApiId the enrichment API ID
     */
    void deleteEnrichmentApi(UUID appId, UUID enrichmentApiId);
    
    /**
     * Deletes the publisher from an app configuration.
     * 
     * @param appId the app ID
     */
    void deletePublisher(UUID appId);
}
