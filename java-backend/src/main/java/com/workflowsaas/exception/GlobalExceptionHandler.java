package com.workflowsaas.exception;

import com.workflowsaas.config.ApplicationProperties;
import com.workflowsaas.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Global exception handler for consistent error responses.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final ApplicationProperties applicationProperties;
    
    @ExceptionHandler(WorkflowSaasException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowSaasException(WorkflowSaasException ex) {
        ErrorResponse response = new ErrorResponse(
            ex.getMessage(),
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("Validation failed");

        log.error("Validation error: {}", message, ex);

        ErrorResponse response = new ErrorResponse(message, isDevelopment() ? getStackTrace(ex) : null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        String exMessage = ex.getMessage();
        
        if (exMessage != null) {
            if (exMessage.contains("unique_app_name_per_tenant")) {
                message = "An app with this name already exists";
            } else if (exMessage.contains("unique")) {
                message = "Duplicate entry - this value already exists";
            }
        }
        
        ErrorResponse response = new ErrorResponse(message, isDevelopment() ? getStackTrace(ex) : null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
            "Internal server error",
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    private boolean isDevelopment() {
        String profile = applicationProperties.getProfile();
        return "development".equals(profile) || "dev".equals(profile);
    }
    
    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
