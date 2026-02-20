package com.workflowsaas.service.impl;

import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.entity.App;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.repository.AppRepository;
import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.security.TenantContextHolder;
import com.workflowsaas.service.ApiKeyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppServiceImpl.
 * Tests all business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AppServiceImplTest {

    @Mock
    private AppRepository appRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private ApiKeyService apiKeyService;

    @InjectMocks
    private AppServiceImpl appService;

    private UUID testTenantId;
    private UUID testAppId;
    private App testApp;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testAppId = UUID.randomUUID();
        
        testApp = new App();
        testApp.setId(testAppId);
        testApp.setTenantId(testTenantId);
        testApp.setName("Test App");
        testApp.setApiKeyHash("hashed_key");
        testApp.setCreatedAt(LocalDateTime.now());
        testApp.setUpdatedAt(LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreateApp_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        CreateAppRequest request = new CreateAppRequest("New App");
        String plainTextKey = "app_abc123xyz";
        String hashedKey = "hashed_abc123xyz";

        when(apiKeyService.generateApiKey()).thenReturn(plainTextKey);
        when(apiKeyService.hashApiKey(plainTextKey)).thenReturn(hashedKey);
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> {
            App app = invocation.getArgument(0);
            app.setId(testAppId);
            return app;
        });

        // Act
        App result = appService.createApp(request);

        // Assert
        assertNotNull(result);
        assertEquals("New App", result.getName());
        assertEquals(testTenantId, result.getTenantId());
        assertEquals(plainTextKey, result.getApiKeyHash()); // Plain text returned for one-time display
        
        verify(apiKeyService).generateApiKey();
        verify(apiKeyService).hashApiKey(plainTextKey);
        verify(appRepository).save(any(App.class));
    }

    @Test
    void testCreateApp_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear(); // No tenant context
        CreateAppRequest request = new CreateAppRequest("New App");
        String plainTextKey = "app_abc123xyz";
        String hashedKey = "hashed_abc123xyz";
        UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        when(apiKeyService.generateApiKey()).thenReturn(plainTextKey);
        when(apiKeyService.hashApiKey(plainTextKey)).thenReturn(hashedKey);
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> {
            App app = invocation.getArgument(0);
            app.setId(testAppId);
            return app;
        });

        // Act
        App result = appService.createApp(request);

        // Assert
        assertNotNull(result);
        assertEquals(defaultTenantId, result.getTenantId());
        verify(appRepository).save(argThat(app -> 
            app.getTenantId().equals(defaultTenantId)
        ));
    }

    @Test
    void testGetAllApps_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        List<App> apps = Arrays.asList(testApp);
        when(appRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId)).thenReturn(apps);

        // Act
        List<App> result = appService.getAllApps();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testApp.getId(), result.get(0).getId());
        verify(appRepository).findByTenantIdOrderByCreatedAtDesc(testTenantId);
    }

    @Test
    void testGetAllApps_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        List<App> apps = Arrays.asList(testApp);
        when(appRepository.findByTenantIdOrderByCreatedAtDesc(defaultTenantId)).thenReturn(apps);

        // Act
        List<App> result = appService.getAllApps();

        // Assert
        assertNotNull(result);
        verify(appRepository).findByTenantIdOrderByCreatedAtDesc(defaultTenantId);
    }

    @Test
    void testGetAllAppsWithWorkflows_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        List<App> apps = Arrays.asList(testApp);
        
        Workflow bpmn = createWorkflow(UUID.randomUUID(), "BPMN Workflow", "BPMN", null);
        Workflow dmn = createWorkflow(UUID.randomUUID(), "DMN Decision", "DMN", bpmn.getId());
        List<Workflow> workflows = Arrays.asList(bpmn, dmn);

        when(appRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId)).thenReturn(apps);
        when(workflowRepository.findByAppIdOrderByCreatedAtDesc(testAppId)).thenReturn(workflows);

        // Act
        List<AppResponse> result = appService.getAllAppsWithWorkflows();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        AppResponse appResponse = result.get(0);
        assertEquals(testApp.getId(), appResponse.id());
        assertEquals(testApp.getName(), appResponse.name());
        assertNotNull(appResponse.workflows());
        assertEquals(1, appResponse.workflows().size()); // Only BPMN at root level
        
        // Verify hierarchical structure
        assertEquals("BPMN Workflow", appResponse.workflows().get(0).name());
        assertEquals(1, appResponse.workflows().get(0).children().size());
        assertEquals("DMN Decision", appResponse.workflows().get(0).children().get(0).name());
    }

    @Test
    void testGetAllAppsWithWorkflows_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        List<App> apps = Arrays.asList(testApp);

        when(appRepository.findByTenantIdOrderByCreatedAtDesc(defaultTenantId)).thenReturn(apps);
        when(workflowRepository.findByAppIdOrderByCreatedAtDesc(any())).thenReturn(new ArrayList<>());

        // Act
        List<AppResponse> result = appService.getAllAppsWithWorkflows();

        // Assert
        assertNotNull(result);
        verify(appRepository).findByTenantIdOrderByCreatedAtDesc(defaultTenantId);
    }

    @Test
    void testGetAppById_Found() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(appRepository.findByIdAndTenantId(testAppId, testTenantId)).thenReturn(Optional.of(testApp));

        // Act
        App result = appService.getAppById(testAppId);

        // Assert
        assertNotNull(result);
        assertEquals(testAppId, result.getId());
        assertEquals(testApp.getName(), result.getName());
        verify(appRepository).findByIdAndTenantId(testAppId, testTenantId);
    }

    @Test
    void testGetAppById_NotFound() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(appRepository.findByIdAndTenantId(testAppId, testTenantId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> appService.getAppById(testAppId)
        );
        
        assertTrue(exception.getMessage().contains("App not found"));
        assertTrue(exception.getMessage().contains(testAppId.toString()));
    }

    @Test
    void testGetAppById_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        UUID defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        when(appRepository.findByIdAndTenantId(testAppId, defaultTenantId)).thenReturn(Optional.of(testApp));

        // Act
        App result = appService.getAppById(testAppId);

        // Assert
        assertNotNull(result);
        verify(appRepository).findByIdAndTenantId(testAppId, defaultTenantId);
    }

    @Test
    void testUpdateApp_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UpdateAppRequest request = new UpdateAppRequest("Updated App Name");
        
        when(appRepository.findByIdAndTenantId(testAppId, testTenantId)).thenReturn(Optional.of(testApp));
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        App result = appService.updateApp(testAppId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated App Name", result.getName());
        verify(appRepository).save(testApp);
    }

    @Test
    void testDeleteApp_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(appRepository.findByIdAndTenantId(testAppId, testTenantId)).thenReturn(Optional.of(testApp));

        // Act
        appService.deleteApp(testAppId);

        // Assert
        verify(appRepository).delete(testApp);
    }

    @Test
    void testRegenerateApiKey_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        String newPlainTextKey = "app_newkey123";
        String newHashedKey = "hashed_newkey123";

        when(appRepository.findByIdAndTenantId(testAppId, testTenantId)).thenReturn(Optional.of(testApp));
        when(apiKeyService.generateApiKey()).thenReturn(newPlainTextKey);
        when(apiKeyService.hashApiKey(newPlainTextKey)).thenReturn(newHashedKey);
        when(appRepository.save(any(App.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String result = appService.regenerateApiKey(testAppId);

        // Assert
        assertEquals(newPlainTextKey, result);
        assertEquals(newHashedKey, testApp.getApiKeyHash());
        verify(apiKeyService).generateApiKey();
        verify(apiKeyService).hashApiKey(newPlainTextKey);
        verify(appRepository).save(testApp);
    }

    @Test
    void testValidateApiKey_Valid() {
        // Arrange
        String apiKey = "app_validkey123";
        List<App> allApps = Arrays.asList(testApp);

        when(appRepository.findAll()).thenReturn(allApps);
        when(apiKeyService.validateApiKey(apiKey, testApp.getApiKeyHash())).thenReturn(true);

        // Act
        App result = appService.validateApiKey(apiKey);

        // Assert
        assertNotNull(result);
        assertEquals(testApp.getId(), result.getId());
        verify(apiKeyService).validateApiKey(apiKey, testApp.getApiKeyHash());
    }

    @Test
    void testValidateApiKey_Invalid() {
        // Arrange
        String apiKey = "app_invalidkey123";
        List<App> allApps = Arrays.asList(testApp);

        when(appRepository.findAll()).thenReturn(allApps);
        when(apiKeyService.validateApiKey(apiKey, testApp.getApiKeyHash())).thenReturn(false);

        // Act
        App result = appService.validateApiKey(apiKey);

        // Assert
        assertNull(result);
    }

    @Test
    void testValidateApiKey_NoApps() {
        // Arrange
        String apiKey = "app_somekey123";
        when(appRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        App result = appService.validateApiKey(apiKey);

        // Assert
        assertNull(result);
    }

    @Test
    void testValidateApiKey_MultipleApps_FindsCorrectOne() {
        // Arrange
        String apiKey = "app_validkey123";
        
        App app1 = new App();
        app1.setId(UUID.randomUUID());
        app1.setApiKeyHash("hash1");
        
        App app2 = new App();
        app2.setId(UUID.randomUUID());
        app2.setApiKeyHash("hash2");
        
        List<App> allApps = Arrays.asList(app1, testApp, app2);

        when(appRepository.findAll()).thenReturn(allApps);
        when(apiKeyService.validateApiKey(apiKey, "hash1")).thenReturn(false);
        when(apiKeyService.validateApiKey(apiKey, testApp.getApiKeyHash())).thenReturn(true);

        // Act
        App result = appService.validateApiKey(apiKey);

        // Assert
        assertNotNull(result);
        assertEquals(testApp.getId(), result.getId());
    }

    // Helper method to create workflow entities
    private Workflow createWorkflow(UUID id, String name, String type, UUID parentId) {
        Workflow workflow = new Workflow();
        workflow.setId(id);
        workflow.setName(name);
        workflow.setType(type);
        workflow.setParentWorkflowId(parentId);
        workflow.setApp(testApp);
        return workflow;
    }
}
