package com.workflowsaas.service.impl;

import com.workflowsaas.dto.request.CreateAppConfigRequest;
import com.workflowsaas.dto.request.EnrichmentApiConfigRequest;
import com.workflowsaas.dto.request.PublisherConfigRequest;
import com.workflowsaas.dto.request.WarehousePublisherRequest;
import com.workflowsaas.dto.response.*;
import com.workflowsaas.entity.AppConfiguration;
import com.workflowsaas.entity.EnrichmentApiConfig;
import com.workflowsaas.entity.PublisherConfig;
import com.workflowsaas.entity.WarehousePublisherConfig;
import com.workflowsaas.exception.ConfigurationNotFoundException;
import com.workflowsaas.exception.UnauthorizedAccessException;
import com.workflowsaas.repository.AppConfigRepository;
import com.workflowsaas.repository.AppRepository;
import com.workflowsaas.repository.EnrichmentApiConfigRepository;
import com.workflowsaas.repository.PublisherConfigRepository;
import com.workflowsaas.repository.WarehousePublisherConfigRepository;
import com.workflowsaas.security.TenantContextHolder;
import com.workflowsaas.service.AppConfigService;
import com.workflowsaas.service.FileValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AppConfigService for managing app configurations.
 */
@Service
@RequiredArgsConstructor
class AppConfigServiceImpl implements AppConfigService {
    
    private final AppConfigRepository appConfigRepository;
    private final EnrichmentApiConfigRepository enrichmentApiConfigRepository;
    private final PublisherConfigRepository publisherConfigRepository;
    private final WarehousePublisherConfigRepository warehousePublisherConfigRepository;
    private final FileValidationService fileValidationService;
    private final AppRepository appRepository;
    
    @Override
    @Transactional(readOnly = true)
    public AppConfigResponse getConfig(UUID appId) {
        // Extract tenant ID from security context
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        // Query repository with tenant isolation
        AppConfiguration config = appConfigRepository.findByAppIdAndTenantId(appId, tenantId)
                .orElseThrow(() -> new ConfigurationNotFoundException(appId));
        
        // Map entity to response DTO
        return mapToResponse(config);
    }
    
    @Override
    @Transactional
    public AppConfigResponse createConfig(UUID appId, CreateAppConfigRequest request) {
        // Extract tenant ID from security context
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        // Validate app belongs to tenant
        appRepository.findByIdAndTenantId(appId, tenantId)
                .orElseThrow(() -> new UnauthorizedAccessException("App does not belong to your tenant"));
        
        // Validate source selection is not empty (already validated by @NotNull, but double-check)
        if (request.source() == null || request.source().isBlank()) {
            throw new IllegalArgumentException("Source selection cannot be empty");
        }
        
        // Validate all file uploads using FileValidationService
        validateFileUploads(request);
        
        // Create AppConfiguration entity
        AppConfiguration config = new AppConfiguration();
        config.setTenantId(tenantId);
        config.setAppId(appId);
        config.setSource(request.source());
        
        // Set parameters file if provided
        if (request.parametersFile() != null) {
            config.setParametersFilename(request.parametersFile().filename());
            config.setParametersContent(request.parametersFile().content());
        }
        
        // Set model file if provided
        if (request.modelFile() != null) {
            config.setModelFilename(request.modelFile().filename());
            config.setModelContent(request.modelFile().content());
        }
        
        // Create enrichment API entities
        if (request.enrichmentApis() != null && !request.enrichmentApis().isEmpty()) {
            for (EnrichmentApiConfigRequest enrichmentApiRequest : request.enrichmentApis()) {
                EnrichmentApiConfig enrichmentApi = createEnrichmentApiEntity(enrichmentApiRequest, config, tenantId);
                config.getEnrichmentApis().add(enrichmentApi);
            }
        }
        
        // Create publisher entity if provided
        if (request.publisher() != null) {
            PublisherConfig publisher = createPublisherEntity(request.publisher(), config, tenantId);
            config.setPublisher(publisher);
        }
        
        // Save to repository and return response DTO
        AppConfiguration savedConfig = appConfigRepository.save(config);
        return mapToResponse(savedConfig);
    }
    
    /**
     * Validates all file uploads in the request using FileValidationService.
     */
    private void validateFileUploads(CreateAppConfigRequest request) {
        // Validate parameters file
        if (request.parametersFile() != null && request.parametersFile().content() != null) {
            fileValidationService.validateCsvContent(request.parametersFile().content());
        }
        
        // Validate model file
        if (request.modelFile() != null && request.modelFile().content() != null) {
            fileValidationService.validateCsvContent(request.modelFile().content());
        }
        
        // Validate enrichment API files
        if (request.enrichmentApis() != null) {
            for (EnrichmentApiConfigRequest enrichmentApi : request.enrichmentApis()) {
                if (enrichmentApi.openApiDocument() != null) {
                    fileValidationService.validateOpenApiDocument(enrichmentApi.openApiDocument());
                }
                if (enrichmentApi.requestMappings() != null && !enrichmentApi.requestMappings().isBlank()) {
                    fileValidationService.validateRequestMappings(enrichmentApi.requestMappings());
                }
            }
        }
        
        // Validate publisher files
        if (request.publisher() != null) {
            PublisherConfigRequest publisher = request.publisher();
            
            // Validate API publisher OpenAPI document
            if ("api".equals(publisher.type()) && publisher.openApiDocument() != null) {
                fileValidationService.validateOpenApiDocument(publisher.openApiDocument());
            }
            
            // Validate API publisher request mappings
            if ("api".equals(publisher.type()) && publisher.requestMappings() != null && !publisher.requestMappings().isBlank()) {
                fileValidationService.validateRequestMappings(publisher.requestMappings());
            }
            
            // Validate warehouse publisher (for EEH type)
            if ("eeh".equals(publisher.type()) && publisher.warehousePublisher() != null) {
                WarehousePublisherRequest warehouse = publisher.warehousePublisher();
                if (warehouse.avroSchema() != null) {
                    fileValidationService.validateAvroSchema(warehouse.avroSchema());
                }
                if (warehouse.mappings() != null && !warehouse.mappings().isBlank()) {
                    fileValidationService.validateRequestMappings(warehouse.mappings());
                }
            }
        }
    }
    
    /**
     * Creates an EnrichmentApiConfig entity from request DTO.
     */
    private EnrichmentApiConfig createEnrichmentApiEntity(
            EnrichmentApiConfigRequest request, 
            AppConfiguration config, 
            UUID tenantId) {
        EnrichmentApiConfig enrichmentApi = new EnrichmentApiConfig();
        enrichmentApi.setTenantId(tenantId);
        enrichmentApi.setAppConfiguration(config);
        enrichmentApi.setOpenApiDocument(request.openApiDocument());
        enrichmentApi.setRequestMappings(request.requestMappings());
        enrichmentApi.setConfigProperties(request.configProperties());
        return enrichmentApi;
    }
    
    /**
     * Creates a PublisherConfig entity from request DTO, including warehouse publisher if present.
     */
    private PublisherConfig createPublisherEntity(
            PublisherConfigRequest request, 
            AppConfiguration config, 
            UUID tenantId) {
        PublisherConfig publisher = new PublisherConfig();
        publisher.setTenantId(tenantId);
        publisher.setAppConfiguration(config);
        publisher.setType(request.type());
        publisher.setOpenApiDocument(request.openApiDocument());
        publisher.setRequestMappings(request.requestMappings());
        publisher.setConfigProperties(request.configProperties());
        
        // Create warehouse publisher if provided (for EEH type)
        if (request.warehousePublisher() != null) {
            WarehousePublisherConfig warehouse = createWarehousePublisherEntity(request.warehousePublisher(), publisher, tenantId);
            publisher.setWarehousePublisher(warehouse);
        }
        
        return publisher;
    }
    
    /**
     * Creates a WarehousePublisherConfig entity from request DTO.
     */
    private WarehousePublisherConfig createWarehousePublisherEntity(
            WarehousePublisherRequest request, 
            PublisherConfig publisher, 
            UUID tenantId) {
        WarehousePublisherConfig warehouse = new WarehousePublisherConfig();
        warehouse.setTenantId(tenantId);
        warehouse.setPublisherConfig(publisher);
        warehouse.setAvroSchema(request.avroSchema());
        warehouse.setMappings(request.mappings());
        return warehouse;
    }
    
    @Override
    @Transactional
    public AppConfigResponse updateConfig(UUID appId, CreateAppConfigRequest request) {
        // Extract tenant ID from security context
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        // Make tenant ID effectively final for lambda
        final UUID finalTenantId = tenantId;
        
        // Validate app belongs to tenant
        appRepository.findByIdAndTenantId(appId, finalTenantId)
                .orElseThrow(() -> new UnauthorizedAccessException("App does not belong to your tenant"));
        
        // Validate source selection is not empty
        if (request.source() == null || request.source().isBlank()) {
            throw new IllegalArgumentException("Source selection cannot be empty");
        }
        
        // Validate all file uploads using FileValidationService
        validateFileUploads(request);
        
        // Retrieve existing configuration or create new one
        AppConfiguration config = appConfigRepository.findByAppIdAndTenantId(appId, finalTenantId)
                .orElseGet(() -> {
                    AppConfiguration newConfig = new AppConfiguration();
                    newConfig.setTenantId(finalTenantId);
                    newConfig.setAppId(appId);
                    return newConfig;
                });
        
        // Update all fields
        config.setSource(request.source());
        
        // Update parameters file
        if (request.parametersFile() != null) {
            config.setParametersFilename(request.parametersFile().filename());
            config.setParametersContent(request.parametersFile().content());
        } else {
            config.setParametersFilename(null);
            config.setParametersContent(null);
        }
        
        // Update model file
        if (request.modelFile() != null) {
            config.setModelFilename(request.modelFile().filename());
            config.setModelContent(request.modelFile().content());
        } else {
            config.setModelFilename(null);
            config.setModelContent(null);
        }
        
        // Handle orphan removal for enrichment APIs
        // Clear existing enrichment APIs and add new ones
        config.getEnrichmentApis().clear();
        if (request.enrichmentApis() != null && !request.enrichmentApis().isEmpty()) {
            for (EnrichmentApiConfigRequest enrichmentApiRequest : request.enrichmentApis()) {
                EnrichmentApiConfig enrichmentApi = createEnrichmentApiEntity(enrichmentApiRequest, config, finalTenantId);
                config.getEnrichmentApis().add(enrichmentApi);
            }
        }
        
        // Update publisher entity
        if (request.publisher() != null) {
            if (config.getPublisher() != null) {
                // Update existing publisher
                PublisherConfig existingPublisher = config.getPublisher();
                existingPublisher.setType(request.publisher().type());
                existingPublisher.setOpenApiDocument(request.publisher().openApiDocument());
                existingPublisher.setRequestMappings(request.publisher().requestMappings());
                existingPublisher.setConfigProperties(request.publisher().configProperties());
                
                // Update or create warehouse publisher
                if (request.publisher().warehousePublisher() != null) {
                    if (existingPublisher.getWarehousePublisher() != null) {
                        // Update existing warehouse publisher
                        WarehousePublisherConfig existingWarehouse = existingPublisher.getWarehousePublisher();
                        existingWarehouse.setAvroSchema(request.publisher().warehousePublisher().avroSchema());
                        existingWarehouse.setMappings(request.publisher().warehousePublisher().mappings());
                    } else {
                        // Create new warehouse publisher
                        WarehousePublisherConfig warehouse = createWarehousePublisherEntity(
                                request.publisher().warehousePublisher(), 
                                existingPublisher, 
                                finalTenantId
                        );
                        existingPublisher.setWarehousePublisher(warehouse);
                    }
                } else {
                    // Remove warehouse publisher if not in request
                    existingPublisher.setWarehousePublisher(null);
                }
            } else {
                // Create new publisher
                PublisherConfig publisher = createPublisherEntity(request.publisher(), config, finalTenantId);
                config.setPublisher(publisher);
            }
        } else {
            // Remove publisher if not in request
            config.setPublisher(null);
        }
        
        // Save to repository and return response DTO
        AppConfiguration savedConfig = appConfigRepository.save(config);
        return mapToResponse(savedConfig);
    }
    
    @Override
        @Transactional
        public void deleteEnrichmentApi(UUID appId, UUID enrichmentApiId) {
            // Extract tenant ID from security context
            UUID tenantId = TenantContextHolder.getTenantId();
            
            // For now, use a default tenant ID if none is set (security disabled)
            if (tenantId == null) {
                tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            }

            // Validate app belongs to tenant
            appRepository.findByIdAndTenantId(appId, tenantId)
                    .orElseThrow(() -> new UnauthorizedAccessException("App does not belong to your tenant"));

            // Validate enrichment API exists and belongs to tenant's app
            AppConfiguration config = appConfigRepository.findByAppIdAndTenantId(appId, tenantId)
                    .orElseThrow(() -> new ConfigurationNotFoundException(appId));

            // Check if enrichment API belongs to this app's configuration
            boolean enrichmentApiExists = config.getEnrichmentApis().stream()
                    .anyMatch(api -> api.getId().equals(enrichmentApiId));

            if (!enrichmentApiExists) {
                throw new UnauthorizedAccessException("Enrichment API does not belong to this app");
            }

            // Delete enrichment API by ID and tenant ID
            enrichmentApiConfigRepository.deleteByIdAndTenantId(enrichmentApiId, tenantId);
        }

    
    @Override
    @Transactional
    public void deletePublisher(UUID appId) {
        // Extract tenant ID from security context
        UUID tenantId = TenantContextHolder.getTenantId();
        
        // For now, use a default tenant ID if none is set (security disabled)
        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        
        // Validate app belongs to tenant
        appRepository.findByIdAndTenantId(appId, tenantId)
                .orElseThrow(() -> new UnauthorizedAccessException("App does not belong to your tenant"));
        
        // Validate publisher exists and belongs to tenant's app
        AppConfiguration config = appConfigRepository.findByAppIdAndTenantId(appId, tenantId)
                .orElseThrow(() -> new ConfigurationNotFoundException(appId));
        
        // Check if publisher exists for this app's configuration
        if (config.getPublisher() == null) {
            throw new ConfigurationNotFoundException("Publisher not found for app: " + appId);
        }
        
        // Delete publisher by app configuration ID and tenant ID
        publisherConfigRepository.deleteByAppConfigurationIdAndTenantId(config.getId(), tenantId);
    }
    
    /**
     * Maps AppConfiguration entity to AppConfigResponse DTO.
     * Includes mapping for all nested entities (enrichment APIs, publisher, warehouse publisher, file responses).
     */
    private AppConfigResponse mapToResponse(AppConfiguration config) {
        return new AppConfigResponse(
                config.getId(),
                config.getAppId(),
                config.getSource(),
                mapEnrichmentApis(config),
                mapParametersFile(config),
                mapModelFile(config),
                mapPublisher(config),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
    
    /**
     * Maps enrichment API entities to response DTOs.
     */
    private java.util.List<EnrichmentApiConfigResponse> mapEnrichmentApis(AppConfiguration config) {
        if (config.getEnrichmentApis() == null) {
            return java.util.Collections.emptyList();
        }
        
        return config.getEnrichmentApis().stream()
                .map(this::mapEnrichmentApi)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps single enrichment API entity to response DTO.
     */
    private EnrichmentApiConfigResponse mapEnrichmentApi(EnrichmentApiConfig enrichmentApi) {
        return new EnrichmentApiConfigResponse(
                enrichmentApi.getId(),
                enrichmentApi.getOpenApiDocument(),
                enrichmentApi.getRequestMappings(),
                enrichmentApi.getConfigProperties()
        );
    }
    
    /**
     * Maps parameters file fields to response DTO.
     */
    private ParametersFileResponse mapParametersFile(AppConfiguration config) {
        if (config.getParametersFilename() == null) {
            return null;
        }
        
        return new ParametersFileResponse(
                config.getParametersFilename(),
                config.getParametersContent()
        );
    }
    
    /**
     * Maps model file fields to response DTO.
     */
    private ModelFileResponse mapModelFile(AppConfiguration config) {
        if (config.getModelFilename() == null) {
            return null;
        }
        
        return new ModelFileResponse(
                config.getModelFilename(),
                config.getModelContent()
        );
    }
    
    /**
     * Maps publisher entity to response DTO, including warehouse publisher if present.
     */
    private PublisherConfigResponse mapPublisher(AppConfiguration config) {
        PublisherConfig publisher = config.getPublisher();
        if (publisher == null) {
            return null;
        }
        
        return new PublisherConfigResponse(
                publisher.getId(),
                publisher.getType(),
                publisher.getOpenApiDocument(),
                publisher.getRequestMappings(),
                publisher.getConfigProperties(),
                mapWarehousePublisher(publisher)
        );
    }
    
    /**
     * Maps warehouse publisher entity to response DTO.
     */
    private WarehousePublisherResponse mapWarehousePublisher(PublisherConfig publisher) {
        WarehousePublisherConfig warehousePublisher = publisher.getWarehousePublisher();
        if (warehousePublisher == null) {
            return null;
        }
        
        return new WarehousePublisherResponse(
                warehousePublisher.getId(),
                warehousePublisher.getAvroSchema(),
                warehousePublisher.getMappings()
        );
    }
}
