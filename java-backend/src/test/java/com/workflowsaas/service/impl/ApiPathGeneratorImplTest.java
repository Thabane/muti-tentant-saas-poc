package com.workflowsaas.service.impl;

import com.workflowsaas.repository.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApiPathGeneratorImpl.
 * Tests API path generation, uniqueness validation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class ApiPathGeneratorImplTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @InjectMocks
    private ApiPathGeneratorImpl apiPathGenerator;

    private UUID testTenantId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
    }

    @Test
    void testGenerateApiPath_BasicServiceName() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 1);

        assertEquals("/" + testTenantId + "/default/user-service/v1", result);
        verify(workflowRepository, times(1)).existsByApiPath(anyString());
    }

    @Test
    void testGenerateApiPath_IncomeServiceExample() {
        // Test specific example from requirements (5.5)
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "sub-service", "Income Service", 1);

        assertEquals("/" + testTenantId + "/sub-service/income-service/v1", result);
    }

    @Test
    void testGenerateApiPath_WithSpecialCharacters() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User@Service#123!", 1);

        assertEquals("/" + testTenantId + "/default/userservice123/v1", result);
    }

    @Test
    void testGenerateApiPath_WithMultipleSpaces() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User   Service   Name", 1);

        assertEquals("/" + testTenantId + "/default/user-service-name/v1", result);
    }

    @Test
    void testGenerateApiPath_WithMixedCase() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "UserService", 1);

        assertEquals("/" + testTenantId + "/default/userservice/v1", result);
    }

    @Test
    void testGenerateApiPath_WithVersion() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 2);

        assertEquals("/" + testTenantId + "/default/user-service/v2", result);
    }

    @Test
    void testGenerateApiPath_NullVersionDefaultsToV1() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", null);

        assertEquals("/" + testTenantId + "/default/user-service/v1", result);
    }

    @Test
    void testGenerateApiPath_ZeroVersionDefaultsToV1() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 0);

        assertEquals("/" + testTenantId + "/default/user-service/v1", result);
    }

    @Test
    void testGenerateApiPath_ConflictResolution() {
        String basePath = "/" + testTenantId + "/default/user-service/v1";
        String expectedPath = "/" + testTenantId + "/default/user-service/v1-2";

        // First path exists, second path is unique
        when(workflowRepository.existsByApiPath(basePath)).thenReturn(true);
        when(workflowRepository.existsByApiPath(expectedPath)).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 1);

        assertEquals(expectedPath, result);
        verify(workflowRepository, times(2)).existsByApiPath(anyString());
    }

    @Test
    void testGenerateApiPath_MultipleConflicts() {
        String basePath = "/" + testTenantId + "/default/user-service/v1";
        String conflict1 = "/" + testTenantId + "/default/user-service/v1-2";
        String conflict2 = "/" + testTenantId + "/default/user-service/v1-3";
        String expectedPath = "/" + testTenantId + "/default/user-service/v1-4";

        when(workflowRepository.existsByApiPath(basePath)).thenReturn(true);
        when(workflowRepository.existsByApiPath(conflict1)).thenReturn(true);
        when(workflowRepository.existsByApiPath(conflict2)).thenReturn(true);
        when(workflowRepository.existsByApiPath(expectedPath)).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 1);

        assertEquals(expectedPath, result);
        verify(workflowRepository, times(4)).existsByApiPath(anyString());
    }

    @Test
    void testGenerateApiPath_MaxAttemptsExceeded() {
        // All paths are taken
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                apiPathGenerator.generateApiPath(testTenantId, "default", "User Service", 1)
        );

        assertTrue(exception.getMessage().contains("Unable to generate unique API path"));
        assertTrue(exception.getMessage().contains("100 attempts"));
    }

    @Test
    void testGenerateApiPath_NullServiceName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                apiPathGenerator.generateApiPath(testTenantId, "default", null, 1)
        );

        assertEquals("Service name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateApiPath_BlankServiceName() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                apiPathGenerator.generateApiPath(testTenantId, "default", "   ", 1)
        );

        assertEquals("Service name cannot be null or blank", exception.getMessage());
    }

    @Test
    void testGenerateApiPath_OnlySpecialCharacters() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                apiPathGenerator.generateApiPath(testTenantId, "default", "@#$%^&*()", 1)
        );

        assertEquals("Service name must contain at least one alphanumeric character", exception.getMessage());
    }

    @Test
    void testIsPathUnique_PathExists() {
        String testPath = "/test/path";
        when(workflowRepository.existsByApiPath(testPath)).thenReturn(true);

        boolean result = apiPathGenerator.isPathUnique(testPath);

        assertFalse(result);
        verify(workflowRepository, times(1)).existsByApiPath(testPath);
    }

    @Test
    void testIsPathUnique_PathDoesNotExist() {
        String testPath = "/test/path";
        when(workflowRepository.existsByApiPath(testPath)).thenReturn(false);

        boolean result = apiPathGenerator.isPathUnique(testPath);

        assertTrue(result);
        verify(workflowRepository, times(1)).existsByApiPath(testPath);
    }

    @Test
    void testGenerateApiPath_WithHyphensInName() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "user-service-api", 1);

        assertEquals("/" + testTenantId + "/default/user-service-api/v1", result);
    }

    @Test
    void testGenerateApiPath_WithNumbers() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "Service123", 1);

        assertEquals("/" + testTenantId + "/default/service123/v1", result);
    }

    @Test
    void testGenerateApiPath_LeadingAndTrailingSpaces() {
        when(workflowRepository.existsByApiPath(anyString())).thenReturn(false);

        String result = apiPathGenerator.generateApiPath(testTenantId, "default", "  User Service  ", 1);

        // Leading/trailing spaces are converted to hyphens, then removed by the regex
        assertEquals("/" + testTenantId + "/default/-user-service-/v1", result);
    }
}
