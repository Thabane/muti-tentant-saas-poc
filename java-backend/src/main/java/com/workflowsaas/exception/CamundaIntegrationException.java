package com.workflowsaas.exception;

/**
 * Exception thrown when Camunda integration fails.
 */
public class CamundaIntegrationException extends WorkflowSaasException {
    
    public CamundaIntegrationException(String message) {
        super(message, 500);
    }
}
