package com.workflowsaas.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission to access.
 */
public class UnauthorizedAccessException extends WorkflowSaasException {
    
    public UnauthorizedAccessException(String reason) {
        super(reason, 403);
    }
}
