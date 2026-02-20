package com.workflowsaas.service.impl;

import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.service.ApiPathGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation of ApiPathGenerator for generating API paths.
 */
@Service
@RequiredArgsConstructor
public class ApiPathGeneratorImpl implements ApiPathGenerator {
    
    private static final int MAX_ATTEMPTS = 100;
    private final WorkflowRepository workflowRepository;
    
    @Override
    public String generateApiPath(UUID tenantId, String subService, String serviceName, Integer version) {
        String normalizedServiceName = normalizeServiceName(serviceName);
        int versionNumber = (version != null && version > 0) ? version : 1;
        String basePath = String.format("/%s/%s/%s/v%d", tenantId, subService, normalizedServiceName, versionNumber);
        
        if (isPathUnique(basePath)) {
            return basePath;
        }
        
        // Handle conflicts with counter suffix
        for (int i = 2; i <= MAX_ATTEMPTS; i++) {
            String pathWithSuffix = String.format("/%s/%s/%s/v%d-%d", tenantId, subService, normalizedServiceName, versionNumber, i);
            if (isPathUnique(pathWithSuffix)) {
                return pathWithSuffix;
            }
        }
        
        throw new IllegalStateException("Unable to generate unique API path after " + MAX_ATTEMPTS + " attempts");
    }
    
    @Override
    public boolean isPathUnique(String apiPath) {
        return !workflowRepository.existsByApiPath(apiPath);
    }
    
    private String normalizeServiceName(String serviceName) {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("Service name cannot be null or blank");
        }
        
        // Convert to lowercase, replace spaces with hyphens, remove special characters except hyphens
        String normalized = serviceName.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "");
        
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Service name must contain at least one alphanumeric character");
        }
        
        return normalized;
    }
}
