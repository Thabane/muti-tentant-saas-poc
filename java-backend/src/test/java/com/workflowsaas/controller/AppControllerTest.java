package com.workflowsaas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowsaas.config.ApplicationProperties;
import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.entity.App;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.service.AppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AppController.
 * Tests controller layer with mocked service.
 * 
 * Note: Excludes Spring Security auto-configuration since security is disabled.
 */
@WebMvcTest(
    controllers = AppController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    }
)
class AppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppService appService;

    @MockBean
    private ApplicationProperties applicationProperties;

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
        testApp.setApiKeyHash("app_12345678901234567890123456789012"); // Exactly 36 chars (app_ + 32)
        testApp.setCreatedAt(now);
        testApp.setUpdatedAt(now);
    }

    @Test
    void testCreateApp_Success() throws Exception {
        // Arrange
        CreateAppRequest request = new CreateAppRequest("Test App");
        when(appService.createApp(any(CreateAppRequest.class))).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(post("/api/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testAppId.toString()))
                .andExpect(jsonPath("$.name").value("Test App"))
                .andExpect(jsonPath("$.apiKey").exists())
                .andExpect(jsonPath("$.apiKey").value(startsWith("app_")))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.workflows").isArray());
        
        verify(appService, times(1)).createApp(any(CreateAppRequest.class));
    }

    @Test
    void testCreateApp_WithBlankName_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateAppRequest request = new CreateAppRequest("");

        // Act & Assert
        mockMvc.perform(post("/api/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateApp_WithNullName_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateAppRequest request = new CreateAppRequest(null);

        // Act & Assert
        mockMvc.perform(post("/api/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllApps_EmptyList() throws Exception {
        // Arrange
        when(appService.getAllAppsWithWorkflows()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/apps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        
        verify(appService, times(1)).getAllAppsWithWorkflows();
    }

    @Test
    void testGetAllApps_WithApps() throws Exception {
        // Arrange
        AppResponse app1 = new AppResponse(UUID.randomUUID(), "App 1", null, now, now, List.of());
        AppResponse app2 = new AppResponse(UUID.randomUUID(), "App 2", null, now, now, List.of());
        when(appService.getAllAppsWithWorkflows()).thenReturn(Arrays.asList(app1, app2));

        // Act & Assert
        mockMvc.perform(get("/api/apps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name").value(containsInAnyOrder("App 1", "App 2")));
        
        verify(appService, times(1)).getAllAppsWithWorkflows();
    }

    @Test
    void testGetApp_Success() throws Exception {
        // Arrange
        when(appService.getAppById(testAppId)).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(get("/api/apps/" + testAppId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAppId.toString()))
                .andExpect(jsonPath("$.name").value("Test App"))
                .andExpect(jsonPath("$.apiKey").doesNotExist());
        
        verify(appService, times(1)).getAppById(testAppId);
    }

    @Test
    void testGetApp_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(appService.getAppById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("App not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(get("/api/apps/" + nonExistentId))
                .andExpect(status().isNotFound());
        
        verify(appService, times(1)).getAppById(nonExistentId);
    }

    @Test
    void testUpdateApp_Success() throws Exception {
        // Arrange
        UpdateAppRequest request = new UpdateAppRequest("Updated Name");
        testApp.setName("Updated Name");
        when(appService.updateApp(eq(testAppId), any(UpdateAppRequest.class))).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(put("/api/apps/" + testAppId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAppId.toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"));
        
        verify(appService, times(1)).updateApp(eq(testAppId), any(UpdateAppRequest.class));
    }

    @Test
    void testUpdateApp_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        UpdateAppRequest request = new UpdateAppRequest("Updated Name");
        when(appService.updateApp(eq(nonExistentId), any(UpdateAppRequest.class)))
                .thenThrow(new ResourceNotFoundException("App not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(put("/api/apps/" + nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        
        verify(appService, times(1)).updateApp(eq(nonExistentId), any(UpdateAppRequest.class));
    }

    @Test
    void testDeleteApp_Success() throws Exception {
        // Arrange
        doNothing().when(appService).deleteApp(testAppId);

        // Act & Assert
        mockMvc.perform(delete("/api/apps/" + testAppId))
                .andExpect(status().isNoContent());
        
        verify(appService, times(1)).deleteApp(testAppId);
    }

    @Test
    void testDeleteApp_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("App not found with id: " + nonExistentId))
                .when(appService).deleteApp(nonExistentId);

        // Act & Assert
        mockMvc.perform(delete("/api/apps/" + nonExistentId))
                .andExpect(status().isNotFound());
        
        verify(appService, times(1)).deleteApp(nonExistentId);
    }

    @Test
    void testRegenerateApiKey_Success() throws Exception {
        // Arrange
        String newApiKey = "app_12345678901234567890123456789012"; // Exactly 36 chars
        when(appService.regenerateApiKey(testAppId)).thenReturn(newApiKey);

        // Act & Assert
        mockMvc.perform(post("/api/apps/" + testAppId + "/regenerate-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").value(newApiKey))
                .andExpect(jsonPath("$.apiKey").value(startsWith("app_")));
        
        verify(appService, times(1)).regenerateApiKey(testAppId);
    }

    @Test
    void testRegenerateApiKey_NotFound() throws Exception {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(appService.regenerateApiKey(nonExistentId))
                .thenThrow(new ResourceNotFoundException("App not found with id: " + nonExistentId));

        // Act & Assert
        mockMvc.perform(post("/api/apps/" + nonExistentId + "/regenerate-key"))
                .andExpect(status().isNotFound());
        
        verify(appService, times(1)).regenerateApiKey(nonExistentId);
    }

    @Test
    void testCreateApp_ApiKeyFormat() throws Exception {
        // Arrange
        CreateAppRequest request = new CreateAppRequest("Test App");
        when(appService.createApp(any(CreateAppRequest.class))).thenReturn(testApp);

        // Act & Assert
        mockMvc.perform(post("/api/apps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.apiKey").value(matchesPattern("^app_[a-zA-Z0-9]{32}$")));
        
        verify(appService, times(1)).createApp(any(CreateAppRequest.class));
    }
}
