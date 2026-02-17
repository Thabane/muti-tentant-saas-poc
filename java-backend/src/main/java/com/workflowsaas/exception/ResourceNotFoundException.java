package com.workflowsaas.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends WorkflowSaasException {
    
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
