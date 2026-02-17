package com.workflowsaas.exception;

/**
 * Exception thrown for invalid request data.
 */
public class BadRequestException extends WorkflowSaasException {
    
    public BadRequestException(String message) {
        super(message, 400);
    }
}
