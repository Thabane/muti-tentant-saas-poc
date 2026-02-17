package com.workflowsaas.exception;

/**
 * Exception thrown when authentication fails.
 */
public class UnauthorizedException extends WorkflowSaasException {
    
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}
