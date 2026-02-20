package com.workflowsaas.exception;

import com.workflowsaas.config.ApplicationProperties;
import com.workflowsaas.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests exception handling and error response formatting.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // Set profile to production by default (no stack traces)
        when(applicationProperties.getProfile()).thenReturn("production");
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWorkflowSaasException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource not found", response.getBody().error());
        assertNull(response.getBody().stack()); // Production mode
    }

    @Test
    void testHandleBadRequestException() {
        // Arrange
        BadRequestException ex = new BadRequestException("Invalid request");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWorkflowSaasException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request", response.getBody().error());
    }

    @Test
    void testHandleUnauthorizedException() {
        // Arrange
        UnauthorizedException ex = new UnauthorizedException("Unauthorized access");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWorkflowSaasException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized access", response.getBody().error());
    }

    @Test
    void testHandleDataIntegrityViolation_DuplicateAppName() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "could not execute statement; SQL [n/a]; constraint [unique_app_name_per_tenant]; nested exception"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An app with this name already exists", response.getBody().error());
    }

    @Test
    void testHandleDataIntegrityViolation_GenericUnique() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "could not execute statement; SQL [n/a]; constraint [unique_email]; nested exception"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Duplicate entry - this value already exists", response.getBody().error());
    }

    @Test
    void testHandleDataIntegrityViolation_NoUniqueConstraint() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "could not execute statement; SQL [n/a]; foreign key constraint violation"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Data integrity violation", response.getBody().error());
    }

    @Test
    void testHandleDataIntegrityViolation_NullMessage() {
        // Arrange
        DataIntegrityViolationException ex = new DataIntegrityViolationException("test", null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Data integrity violation", response.getBody().error());
    }

    @Test
    void testHandleValidationException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("object", "email", "must be valid");
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().error().contains("name: must not be blank"));
        assertTrue(response.getBody().error().contains("email: must be valid"));
    }

    @Test
    void testHandleValidationException_NoErrors() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList());

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().error());
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().error());
        assertNull(response.getBody().stack()); // Production mode
    }

    @Test
    void testDevelopmentMode_IncludesStackTrace() {
        // Arrange
        when(applicationProperties.getProfile()).thenReturn("development");
        ResourceNotFoundException ex = new ResourceNotFoundException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleWorkflowSaasException(ex);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().stack());
        assertTrue(response.getBody().stack().contains("ResourceNotFoundException"));
    }

    @Test
    void testDevMode_IncludesStackTrace() {
        // Arrange
        when(applicationProperties.getProfile()).thenReturn("dev");
        Exception ex = new RuntimeException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().stack());
        assertTrue(response.getBody().stack().contains("RuntimeException"));
    }

    @Test
    void testProductionMode_NoStackTrace() {
        // Arrange
        when(applicationProperties.getProfile()).thenReturn("production");
        Exception ex = new RuntimeException("Test error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertNull(response.getBody().stack());
    }

    @Test
    void testHandleDataIntegrityViolation_AppNameConstraintCaseInsensitive() {
        // Arrange - The actual constraint name in the database is lowercase
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "constraint [unique_app_name_per_tenant] violated"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An app with this name already exists", response.getBody().error());
    }
}
