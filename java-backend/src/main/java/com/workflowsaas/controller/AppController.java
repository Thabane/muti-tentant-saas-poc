package com.workflowsaas.controller;

import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.ApiKeyResponse;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.entity.App;
import com.workflowsaas.service.AppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for app management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/apps")
@RequiredArgsConstructor
public class AppController {
    
    private final AppService appService;
    
    @PostMapping
    public ResponseEntity<AppResponse> createApp(@Valid @RequestBody CreateAppRequest request) {
        log.debug("Received createApp request: {}", request);
        log.debug("App name: {}, length: {}, isBlank: {}", 
                request.name(), 
                request.name() != null ? request.name().length() : "null",
                request.name() == null || request.name().isBlank());
        
        App app = appService.createApp(request);
        
        log.info("App created successfully with ID: {}", app.getId());
        
        // Build response with plain-text API key (only shown once)
        AppResponse response = new AppResponse(
                app.getId(),
                app.getName(),
                app.getApiKeyHash(), // Contains plain-text key temporarily
                app.getCreatedAt(),
                app.getUpdatedAt(),
                List.of()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<List<AppResponse>> getAllApps() {
        List<AppResponse> apps = appService.getAllAppsWithWorkflows();
        return ResponseEntity.ok(apps);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AppResponse> getApp(@PathVariable UUID id) {
        App app = appService.getAppById(id);
        
        AppResponse response = new AppResponse(
                app.getId(),
                app.getName(),
                null, // Don't expose API key
                app.getCreatedAt(),
                app.getUpdatedAt(),
                List.of()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AppResponse> updateApp(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppRequest request) {
        App app = appService.updateApp(id, request);
        
        AppResponse response = new AppResponse(
                app.getId(),
                app.getName(),
                null,
                app.getCreatedAt(),
                app.getUpdatedAt(),
                List.of()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApp(@PathVariable UUID id) {
        appService.deleteApp(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/regenerate-key")
    public ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable UUID id) {
        String newApiKey = appService.regenerateApiKey(id);
        return ResponseEntity.ok(new ApiKeyResponse(newApiKey));
    }
}
