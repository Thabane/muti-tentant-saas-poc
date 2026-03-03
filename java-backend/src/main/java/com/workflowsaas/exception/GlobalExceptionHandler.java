package com.workflowsaas.exception;

import com.workflowsaas.config.ApplicationProperties;
import com.workflowsaas.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler {
    
    private final ApplicationProperties applicationProperties;
    
    @ExceptionHandler(WorkflowSaasException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowSaasException(WorkflowSaasException ex) {
        log.error("WorkflowSaasException: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse(
            ex.getMessage(),
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        log.error("Validation error: {}", errors, ex);
        
        ErrorResponse response = new ErrorResponse(
            "Validation failed",
            errors,
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(FileValidationException.class)
    public ResponseEntity<ErrorResponse> handleFileValidationException(FileValidationException ex) {
        log.error("File validation error: {}", ex.getMessage(), ex);
        Map<String, String> errors = Map.of("file", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            "File validation failed",
            errors,
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(ConfigurationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleConfigurationNotFoundException(ConfigurationNotFoundException ex) {
        log.error("Configuration not found: {}", ex.getMessage(), ex);
        Map<String, String> errors = Map.of("appId", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            "Configuration not found",
            errors,
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        log.error("Unauthorized access: {}", ex.getMessage(), ex);
        Map<String, String> errors = Map.of("reason", ex.getMessage());
        ErrorResponse response = new ErrorResponse(
            "Access denied",
            errors,
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
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
        
        log.error("Data integrity violation: {}", message, ex);
        
        ErrorResponse response = new ErrorResponse(message, isDevelopment() ? getStackTrace(ex) : null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
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
