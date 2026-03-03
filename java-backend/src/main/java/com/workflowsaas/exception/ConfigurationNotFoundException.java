package com.workflowsaas.exception;

import java.util.UUID;

/**
 * Exception thrown when an app configuration is not found.
 */
public class ConfigurationNotFoundException extends WorkflowSaasException {
    
    public ConfigurationNotFoundException(UUID appId) {
        super("Configuration not found for app ID: " + appId, 404);
    }
    
    public ConfigurationNotFoundException(String message) {
        super(message, 404);
    }
}
