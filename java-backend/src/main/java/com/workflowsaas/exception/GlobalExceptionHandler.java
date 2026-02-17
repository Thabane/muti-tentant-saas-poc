package com.workflowsaas.exception;

import com.workflowsaas.dto.response.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
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
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:production}")
    private String activeProfile;
    
    @ExceptionHandler(WorkflowSaasException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowSaasException(WorkflowSaasException ex) {
        ErrorResponse response = new ErrorResponse(
            ex.getMessage(),
            isDevelopment() ? getStackTrace(ex) : null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data integrity violation";
        if (ex.getMessage().contains("unique")) {
            message = "Duplicate entry - email or slug already exists";
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
        return "development".equals(activeProfile) || "dev".equals(activeProfile);
    }
    
    private String getStackTrace(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
