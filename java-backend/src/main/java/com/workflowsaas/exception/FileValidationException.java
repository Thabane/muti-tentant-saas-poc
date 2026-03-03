package com.workflowsaas.exception;

/**
 * Exception thrown when file validation fails.
 * Used for validating uploaded configuration files (OpenAPI, Avro, CSV).
 */
public class FileValidationException extends WorkflowSaasException {
    
    public FileValidationException(String message) {
        super(message, 400);
    }
    
    public FileValidationException(String message, Throwable cause) {
        super(message, 400);
        initCause(cause);
    }
}
