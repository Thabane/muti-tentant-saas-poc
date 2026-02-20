package com.workflowsaas.controller;

import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.ApiKeyResponse;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.entity.App;
import com.workflowsaas.service.AppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppController.
 * Tests controller logic with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
class AppControllerUnitTest {

    @Mock
    private AppService appService;

    @InjectMocks
    private AppController appController;

    private UUID testAppId;
    private App testApp;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testAppId = UUID.randomUUID();
        now = LocalDateTime.now();
        
        testApp = new App();
        testApp.setId(testAppId);
        testApp.setName("Test App");
        testApp.setApiKeyHash("app_abc123xyz"); // Plain text for one-time display
        testApp.setCreatedAt(now);
        testApp.setUpdatedAt(now);
    }

    @Test
    void testCreateApp_Success() {
        // Arrange
        CreateAppRequest request = new CreateAppRequest("New App");
        when(appService.createApp(any(CreateAppRequest.class))).thenReturn(testApp);

        // Act
        ResponseEntity<AppResponse> response = appController.createApp(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAppId, response.getBody().id());
        assertEquals("Test App", response.getBody().name());
        assertEquals("app_abc123xyz", response.getBody().apiKey());
        
        verify(appService, times(1)).createApp(request);
    }

    @Test
    void testCreateApp_WithValidationError() {
        // Arrange
        CreateAppRequest request = new CreateAppRequest("");
        when(appService.createApp(any(CreateAppRequest.class)))
                .thenThrow(new IllegalArgumentException("App name is required"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> appController.createApp(request));
        verify(appService, times(1)).createApp(request);
    }

    @Test
    void testGetAllApps_Success() {
        // Arrange
        AppResponse appResponse = new AppResponse(
                testAppId,
                "Test App",
                null, // No API key in list response
                now,
                now,
                List.of()
        );
        when(appService.getAllAppsWithWorkflows()).thenReturn(Arrays.asList(appResponse));

        // Act
        ResponseEntity<List<AppResponse>> response = appController.getAllApps();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testAppId, response.getBody().get(0).id());
        
        verify(appService, times(1)).getAllAppsWithWorkflows();
    }

    @Test
    void testGetAllApps_EmptyList() {
        // Arrange
        when(appService.getAllAppsWithWorkflows()).thenReturn(List.of());

        // Act
        ResponseEntity<List<AppResponse>> response = appController.getAllApps();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetApp_Success() {
        // Arrange
        when(appService.getAppById(testAppId)).thenReturn(testApp);

        // Act
        ResponseEntity<AppResponse> response = appController.getApp(testAppId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testAppId, response.getBody().id());
        assertEquals("Test App", response.getBody().name());
        
        verify(appService, times(1)).getAppById(testAppId);
    }

    @Test
    void testUpdateApp_Success() {
        // Arrange
        UpdateAppRequest request = new UpdateAppRequest("Updated App");
        testApp.setName("Updated App");
        when(appService.updateApp(eq(testAppId), any(UpdateAppRequest.class))).thenReturn(testApp);

        // Act
        ResponseEntity<AppResponse> response = appController.updateApp(testAppId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated App", response.getBody().name());
        
        verify(appService, times(1)).updateApp(testAppId, request);
    }

    @Test
    void testDeleteApp_Success() {
        // Arrange
        doNothing().when(appService).deleteApp(testAppId);

        // Act
        ResponseEntity<Void> response = appController.deleteApp(testAppId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        verify(appService, times(1)).deleteApp(testAppId);
    }

    @Test
    void testRegenerateApiKey_Success() {
        // Arrange
        String newApiKey = "app_newkey456";
        when(appService.regenerateApiKey(testAppId)).thenReturn(newApiKey);

        // Act
        ResponseEntity<ApiKeyResponse> response = appController.regenerateApiKey(testAppId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newApiKey, response.getBody().apiKey());
        
        verify(appService, times(1)).regenerateApiKey(testAppId);
    }
}
