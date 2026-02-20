package com.workflowsaas.service;

import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.entity.App;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing apps.
 */
public interface AppService {
    
    /**
     * Creates a new app with generated API key.
     * 
     * @param request the create app request
     * @return created app with plain-text API key
     */
    App createApp(CreateAppRequest request);
    
    /**
     * Gets all apps for the current tenant.
     * 
     * @return list of apps
     */
    List<App> getAllApps();
    
    /**
     * Gets all apps with hierarchical workflows for the current tenant.
     * 
     * @return list of app responses with nested workflows
     */
    List<AppResponse> getAllAppsWithWorkflows();
    
    /**
     * Gets an app by ID for the current tenant.
     * 
     * @param id the app ID
     * @return the app
     */
    App getAppById(UUID id);
    
    /**
     * Updates an app.
     * 
     * @param id the app ID
     * @param request the update request
     * @return updated app
     */
    App updateApp(UUID id, UpdateAppRequest request);
    
    /**
     * Deletes an app.
     * 
     * @param id the app ID
     */
    void deleteApp(UUID id);
    
    /**
     * Regenerates API key for an app.
     * 
     * @param appId the app ID
     * @return new plain-text API key
     */
    String regenerateApiKey(UUID appId);
    
    /**
     * Validates an API key.
     * 
     * @param apiKey the API key to validate
     * @return the app if valid, null otherwise
     */
    App validateApiKey(String apiKey);
}
