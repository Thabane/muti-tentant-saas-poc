package com.workflowsaas.exception;

import lombok.Getter;

/**
 * Base exception class for all application exceptions.
 */
@Getter
public class WorkflowSaasException extends RuntimeException {
    
    private final int statusCode;
    
    public WorkflowSaasException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
