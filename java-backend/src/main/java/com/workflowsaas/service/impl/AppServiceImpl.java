package com.workflowsaas.service.impl;

import com.workflowsaas.dto.request.CreateAppRequest;
import com.workflowsaas.dto.request.UpdateAppRequest;
import com.workflowsaas.dto.response.AppResponse;
import com.workflowsaas.dto.response.WorkflowSummary;
import com.workflowsaas.entity.App;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.exception.UnauthorizedException;
import com.workflowsaas.repository.AppRepository;
import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.security.TenantContextHolder;
import com.workflowsaas.service.ApiKeyService;
import com.workflowsaas.service.AppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AppService for managing apps.
 */
@Service
@RequiredArgsConstructor
public class AppServiceImpl implements AppService {
    
    private final AppRepository appRepository;
    private final WorkflowRepository workflowRepository;
    private final ApiKeyService apiKeyService;
    
    @Override
    @Transactional
    public App createApp(CreateAppRequest request) {
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            // TODO: Implement proper tenant selection when security is re-enabled
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        // Generate API key
        String plainTextApiKey = apiKeyService.generateApiKey();
        String hashedApiKey = apiKeyService.hashApiKey(plainTextApiKey);
        
        App app = new App();
        app.setTenantId(tenantId);
        app.setName(request.name());
        app.setApiKeyHash(hashedApiKey);
        
        App savedApp = appRepository.save(app);
        
        // Store plain-text key temporarily for response (will be cleared after response)
        // In production, consider using a transient field or separate DTO
        savedApp.setApiKeyHash(plainTextApiKey); // Temporarily store plain text for response
        
        return savedApp;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<App> getAllApps() {
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        return appRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getAllAppsWithWorkflows() {
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        List<App> apps = appRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        
        return apps.stream()
                .map(this::buildAppResponse)
                .collect(Collectors.toList());
    }
    
    private AppResponse buildAppResponse(App app) {
        List<Workflow> workflows = workflowRepository.findByAppIdOrderByCreatedAtDesc(app.getId());
        
        // Group workflows by parent (BPMN) and children (DMN)
        Map<UUID, List<Workflow>> childrenByParent = workflows.stream()
                .filter(w -> w.getParentWorkflowId() != null)
                .collect(Collectors.groupingBy(Workflow::getParentWorkflowId));
        
        // Build hierarchical structure
        List<WorkflowSummary> workflowSummaries = workflows.stream()
                .filter(w -> w.getParentWorkflowId() == null) // Only root workflows (BPMNs)
                .map(bpmn -> {
                    List<WorkflowSummary> children = childrenByParent.getOrDefault(bpmn.getId(), new ArrayList<>())
                            .stream()
                            .map(dmn -> new WorkflowSummary(
                                    dmn.getId(),
                                    dmn.getName(),
                                    dmn.getType(),
                                    dmn.getApiPath()
                            ))
                            .collect(Collectors.toList());
                    
                    return new WorkflowSummary(
                            bpmn.getId(),
                            bpmn.getName(),
                            bpmn.getType(),
                            bpmn.getApiPath(),
                            children
                    );
                })
                .collect(Collectors.toList());
        
        return new AppResponse(
                app.getId(),
                app.getName(),
                null, // Don't expose API key hash in list
                app.getCreatedAt(),
                app.getUpdatedAt(),
                workflowSummaries
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public App getAppById(UUID id) {
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        return appRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found with id: " + id));
    }
    
    @Override
    @Transactional
    public App updateApp(UUID id, UpdateAppRequest request) {
        App app = getAppById(id);
        app.setName(request.name());
        return appRepository.save(app);
    }
    
    @Override
    @Transactional
    public void deleteApp(UUID id) {
        App app = getAppById(id);
        appRepository.delete(app);
    }
    
    @Override
    @Transactional
    public String regenerateApiKey(UUID appId) {
        App app = getAppById(appId);
        
        // Generate new API key
        String newPlainTextApiKey = apiKeyService.generateApiKey();
        String newHashedApiKey = apiKeyService.hashApiKey(newPlainTextApiKey);
        
        app.setApiKeyHash(newHashedApiKey);
        appRepository.save(app);
        
        return newPlainTextApiKey;
    }
    
    @Override
    @Transactional(readOnly = true)
    public App validateApiKey(String apiKey) {
        // First, hash the provided key to search
        // Note: BCrypt doesn't support reverse lookup, so we need to fetch and validate
        List<App> allApps = appRepository.findAll();
        
        for (App app : allApps) {
            if (apiKeyService.validateApiKey(apiKey, app.getApiKeyHash())) {
                return app;
            }
        }
        
        return null;
    }
}
