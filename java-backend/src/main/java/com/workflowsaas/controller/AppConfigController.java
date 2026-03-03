package com.workflowsaas.controller;

import com.workflowsaas.dto.request.CreateAppConfigRequest;
import com.workflowsaas.dto.response.AppConfigResponse;
import com.workflowsaas.service.AppConfigService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for app configuration management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/apps/{appId}/config")
class AppConfigController {
    
    private final AppConfigService appConfigService;
    
    AppConfigController(AppConfigService appConfigService) {
        this.appConfigService = appConfigService;
    }
    
    @GetMapping
    ResponseEntity<AppConfigResponse> getConfig(@PathVariable UUID appId) {
        log.debug("Getting configuration for app: {}", appId);
        AppConfigResponse response = appConfigService.getConfig(appId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    ResponseEntity<AppConfigResponse> createConfig(
            @PathVariable UUID appId,
            @Valid @RequestBody CreateAppConfigRequest request) {
        log.debug("Creating configuration for app: {}", appId);
        AppConfigResponse response = appConfigService.createConfig(appId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    ResponseEntity<AppConfigResponse> updateConfig(
            @PathVariable UUID appId,
            @Valid @RequestBody CreateAppConfigRequest request) {
        log.debug("Updating configuration for app: {}", appId);
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/enrichment-apis/{enrichmentApiId}")
    ResponseEntity<Void> deleteEnrichmentApi(
            @PathVariable UUID appId,
            @PathVariable UUID enrichmentApiId) {
        log.debug("Deleting enrichment API {} from app: {}", enrichmentApiId, appId);
        appConfigService.deleteEnrichmentApi(appId, enrichmentApiId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/publisher")
    ResponseEntity<Void> deletePublisher(@PathVariable UUID appId) {
        log.debug("Deleting publisher from app: {}", appId);
        appConfigService.deletePublisher(appId);
        return ResponseEntity.noContent().build();
    }
}
