package com.workflowsaas.service.impl;

import com.workflowsaas.dto.response.*;
import com.workflowsaas.entity.*;
import com.workflowsaas.exception.ConfigurationNotFoundException;
import com.workflowsaas.repository.AppConfigRepository;
import com.workflowsaas.repository.EnrichmentApiConfigRepository;
import com.workflowsaas.repository.PublisherConfigRepository;
import com.workflowsaas.repository.WarehousePublisherConfigRepository;
import com.workflowsaas.security.TenantContextHolder;
import com.workflowsaas.service.AppConfigService;
import com.workflowsaas.service.FileValidationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppConfigServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AppConfigServiceImplTest {
    
    @Mock
    private AppConfigRepository appConfigRepository;
    
    @Mock
    private EnrichmentApiConfigRepository enrichmentApiConfigRepository;
    
    @Mock
    private PublisherConfigRepository publisherConfigRepository;
    
    @Mock
    private WarehousePublisherConfigRepository warehousePublisherConfigRepository;
    
    @Mock
    private FileValidationService fileValidationService;
    
    @Mock
    private com.workflowsaas.repository.AppRepository appRepository;
    
    @InjectMocks
    private AppConfigServiceImpl appConfigService;
    
    private UUID tenantId;
    private UUID appId;
    private UUID configId;
    
    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        appId = UUID.randomUUID();
        configId = UUID.randomUUID();
        
        // Set tenant context for all tests
        TenantContextHolder.setTenantId(tenantId);
    }
    
    @AfterEach
    void tearDown() {
        // Clear tenant context after each test
        TenantContextHolder.clear();
    }
    
    @Test
    void getConfig_WithValidAppId_ReturnsConfiguration() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response);
        assertEquals(configId, response.id());
        assertEquals(appId, response.appId());
        assertEquals("api", response.source());
        assertNotNull(response.createdAt());
        assertNotNull(response.updatedAt());
        
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
    }
    
    @Test
    void getConfig_WithNonExistentAppId_ThrowsConfigurationNotFoundException() {
        // Arrange
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ConfigurationNotFoundException.class, 
                () -> appConfigService.getConfig(appId));
        
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
    }
    
    @Test
    void getConfig_WithEnrichmentApis_MapsAllEnrichmentApis() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        
        EnrichmentApiConfig enrichmentApi1 = new EnrichmentApiConfig();
        enrichmentApi1.setId(UUID.randomUUID());
        enrichmentApi1.setOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        enrichmentApi1.setRequestMappings("source1 -> target1");
        enrichmentApi1.setConfigProperties(Map.of("key1", "value1"));
        
        EnrichmentApiConfig enrichmentApi2 = new EnrichmentApiConfig();
        enrichmentApi2.setId(UUID.randomUUID());
        enrichmentApi2.setOpenApiDocument("{\"openapi\": \"3.1.0\"}");
        enrichmentApi2.setRequestMappings("source2 -> target2");
        enrichmentApi2.setConfigProperties(Map.of("key2", "value2"));
        
        config.setEnrichmentApis(List.of(enrichmentApi1, enrichmentApi2));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.enrichmentApis());
        assertEquals(2, response.enrichmentApis().size());
        
        EnrichmentApiConfigResponse api1Response = response.enrichmentApis().get(0);
        assertEquals(enrichmentApi1.getId(), api1Response.id());
        assertEquals(enrichmentApi1.getOpenApiDocument(), api1Response.openApiDocument());
        assertEquals(enrichmentApi1.getRequestMappings(), api1Response.requestMappings());
        assertEquals(enrichmentApi1.getConfigProperties(), api1Response.configProperties());
        
        EnrichmentApiConfigResponse api2Response = response.enrichmentApis().get(1);
        assertEquals(enrichmentApi2.getId(), api2Response.id());
    }
    
    @Test
    void getConfig_WithParametersFile_MapsParametersFile() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setParametersFilename("parameters.csv");
        config.setParametersContent("col1,col2\nval1,val2");
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.parametersFile());
        assertEquals("parameters.csv", response.parametersFile().filename());
        assertEquals("col1,col2\nval1,val2", response.parametersFile().content());
    }
    
    @Test
    void getConfig_WithModelFile_MapsModelFile() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setModelFilename("model.csv");
        config.setModelContent("col1,col2\nval1,val2");
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.modelFile());
        assertEquals("model.csv", response.modelFile().filename());
        assertEquals("col1,col2\nval1,val2", response.modelFile().content());
    }
    
    @Test
    void getConfig_WithApiPublisher_MapsPublisher() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        
        PublisherConfig publisher = new PublisherConfig();
        publisher.setId(UUID.randomUUID());
        publisher.setType("api");
        publisher.setOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        publisher.setRequestMappings("source -> target");
        publisher.setConfigProperties(Map.of("key", "value"));
        
        config.setPublisher(publisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.publisher());
        assertEquals(publisher.getId(), response.publisher().id());
        assertEquals("api", response.publisher().type());
        assertEquals(publisher.getOpenApiDocument(), response.publisher().openApiDocument());
        assertEquals(publisher.getRequestMappings(), response.publisher().requestMappings());
        assertEquals(publisher.getConfigProperties(), response.publisher().configProperties());
        assertNull(response.publisher().warehousePublisher());
    }
    
    @Test
    void getConfig_WithEehPublisherAndWarehousePublisher_MapsWarehousePublisher() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        
        PublisherConfig publisher = new PublisherConfig();
        publisher.setId(UUID.randomUUID());
        publisher.setType("eeh");
        
        WarehousePublisherConfig warehousePublisher = new WarehousePublisherConfig();
        warehousePublisher.setId(UUID.randomUUID());
        warehousePublisher.setAvroSchema("{\"type\": \"record\"}");
        warehousePublisher.setMappings("source -> target");
        
        publisher.setWarehousePublisher(warehousePublisher);
        config.setPublisher(publisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.publisher());
        assertEquals("eeh", response.publisher().type());
        assertNotNull(response.publisher().warehousePublisher());
        assertEquals(warehousePublisher.getId(), response.publisher().warehousePublisher().id());
        assertEquals(warehousePublisher.getAvroSchema(), response.publisher().warehousePublisher().avroSchema());
        assertEquals(warehousePublisher.getMappings(), response.publisher().warehousePublisher().mappings());
    }
    
    @Test
    void getConfig_WithNullEnrichmentApis_ReturnsEmptyList() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setEnrichmentApis(null);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNotNull(response.enrichmentApis());
        assertTrue(response.enrichmentApis().isEmpty());
    }
    
    @Test
    void getConfig_WithNoParametersFile_ReturnsNullParametersFile() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setParametersFilename(null);
        config.setParametersContent(null);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNull(response.parametersFile());
    }
    
    @Test
    void getConfig_WithNoModelFile_ReturnsNullModelFile() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setModelFilename(null);
        config.setModelContent(null);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNull(response.modelFile());
    }
    
    @Test
    void getConfig_WithNoPublisher_ReturnsNullPublisher() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        config.setPublisher(null);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        AppConfigResponse response = appConfigService.getConfig(appId);
        
        // Assert
        assertNull(response.publisher());
    }
    
    @Test
    void getConfig_UsesTenantIdFromContext() {
        // Arrange
        AppConfiguration config = createTestConfiguration();
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        appConfigService.getConfig(appId);
        
        // Assert - verify tenant ID from context was used
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
    }
    
    /**
     * Helper method to create a basic test configuration.
     */
    private AppConfiguration createTestConfiguration() {
        AppConfiguration config = new AppConfiguration();
        config.setId(configId);
        config.setTenantId(tenantId);
        config.setAppId(appId);
        config.setSource("api");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setEnrichmentApis(new ArrayList<>());
        return config;
    }
    
    @Test
    void createConfig_WithValidRequest_CreatesConfiguration() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration savedConfig = createTestConfiguration();
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(savedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.createConfig(appId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(appId, response.appId());
        assertEquals("api", response.source());
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void createConfig_WithNonExistentApp_ThrowsUnauthorizedAccessException() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                null
            );
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(com.workflowsaas.exception.UnauthorizedAccessException.class,
                () -> appConfigService.createConfig(appId, request));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository, never()).save(any());
    }
    
    @Test
    void createConfig_WithParametersFile_ValidatesAndStoresFile() {
        // Arrange
        com.workflowsaas.dto.request.ParametersFileRequest parametersFile = 
            new com.workflowsaas.dto.request.ParametersFileRequest(
                "parameters.csv",
                "col1,col2\nval1,val2"
            );
        
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                parametersFile,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration savedConfig = createTestConfiguration();
        savedConfig.setParametersFilename("parameters.csv");
        savedConfig.setParametersContent("col1,col2\nval1,val2");
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(savedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.createConfig(appId, request);
        
        // Assert
        assertNotNull(response.parametersFile());
        assertEquals("parameters.csv", response.parametersFile().filename());
        
        verify(fileValidationService).validateCsvContent("col1,col2\nval1,val2");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void createConfig_WithEnrichmentApis_ValidatesAndCreatesEntities() {
        // Arrange
        com.workflowsaas.dto.request.EnrichmentApiConfigRequest enrichmentApiRequest = 
            new com.workflowsaas.dto.request.EnrichmentApiConfigRequest(
                "{\"openapi\": \"3.0.0\"}",
                "source -> target",
                Map.of("key", "value")
            );
        
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                List.of(enrichmentApiRequest),
                null,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration savedConfig = createTestConfiguration();
        EnrichmentApiConfig enrichmentApi = new EnrichmentApiConfig();
        enrichmentApi.setId(UUID.randomUUID());
        enrichmentApi.setOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        enrichmentApi.setRequestMappings("source -> target");
        enrichmentApi.setConfigProperties(Map.of("key", "value"));
        savedConfig.setEnrichmentApis(List.of(enrichmentApi));
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(savedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.createConfig(appId, request);
        
        // Assert
        assertNotNull(response.enrichmentApis());
        assertEquals(1, response.enrichmentApis().size());
        
        verify(fileValidationService).validateOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        verify(fileValidationService).validateRequestMappings("source -> target");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithExistingConfiguration_UpdatesAllFields() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "eeh",
                null,
                new com.workflowsaas.dto.request.ParametersFileRequest("new-params.csv", "a,b\n1,2"),
                new com.workflowsaas.dto.request.ModelFileRequest("new-model.csv", "x,y\n3,4"),
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration existingConfig = createTestConfiguration();
        existingConfig.setSource("api");
        existingConfig.setParametersFilename("old-params.csv");
        existingConfig.setParametersContent("old content");
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(existingConfig));
        
        AppConfiguration updatedConfig = createTestConfiguration();
        updatedConfig.setSource("eeh");
        updatedConfig.setParametersFilename("new-params.csv");
        updatedConfig.setParametersContent("a,b\n1,2");
        updatedConfig.setModelFilename("new-model.csv");
        updatedConfig.setModelContent("x,y\n3,4");
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(updatedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals("eeh", response.source());
        assertEquals("new-params.csv", response.parametersFile().filename());
        assertEquals("new-model.csv", response.modelFile().filename());
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(fileValidationService).validateCsvContent("a,b\n1,2");
        verify(fileValidationService).validateCsvContent("x,y\n3,4");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithNonExistentConfiguration_CreatesNewConfiguration() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        AppConfiguration savedConfig = createTestConfiguration();
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(savedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(appId, response.appId());
        assertEquals("api", response.source());
        
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithEnrichmentApis_HandlesOrphanRemoval() {
        // Arrange
        com.workflowsaas.dto.request.EnrichmentApiConfigRequest newEnrichmentApi = 
            new com.workflowsaas.dto.request.EnrichmentApiConfigRequest(
                "{\"openapi\": \"3.1.0\"}",
                "new -> mapping",
                Map.of("new", "config")
            );
        
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                List.of(newEnrichmentApi),
                null,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration existingConfig = createTestConfiguration();
        EnrichmentApiConfig oldEnrichmentApi = new EnrichmentApiConfig();
        oldEnrichmentApi.setId(UUID.randomUUID());
        oldEnrichmentApi.setOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        existingConfig.setEnrichmentApis(new ArrayList<>(List.of(oldEnrichmentApi)));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(existingConfig));
        
        AppConfiguration updatedConfig = createTestConfiguration();
        EnrichmentApiConfig newApiEntity = new EnrichmentApiConfig();
        newApiEntity.setId(UUID.randomUUID());
        newApiEntity.setOpenApiDocument("{\"openapi\": \"3.1.0\"}");
        newApiEntity.setRequestMappings("new -> mapping");
        updatedConfig.setEnrichmentApis(List.of(newApiEntity));
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(updatedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.enrichmentApis().size());
        assertEquals("{\"openapi\": \"3.1.0\"}", response.enrichmentApis().get(0).openApiDocument());
        
        verify(fileValidationService).validateOpenApiDocument("{\"openapi\": \"3.1.0\"}");
        verify(fileValidationService).validateRequestMappings("new -> mapping");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithPublisher_UpdatesExistingPublisher() {
        // Arrange
        com.workflowsaas.dto.request.PublisherConfigRequest publisherRequest = 
            new com.workflowsaas.dto.request.PublisherConfigRequest(
                "api",
                "{\"openapi\": \"3.1.0\"}",
                "updated -> mapping",
                Map.of("updated", "config"),
                null
            );
        
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                publisherRequest
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration existingConfig = createTestConfiguration();
        PublisherConfig existingPublisher = new PublisherConfig();
        existingPublisher.setId(UUID.randomUUID());
        existingPublisher.setType("api");
        existingPublisher.setOpenApiDocument("{\"openapi\": \"3.0.0\"}");
        existingConfig.setPublisher(existingPublisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(existingConfig));
        
        AppConfiguration updatedConfig = createTestConfiguration();
        PublisherConfig updatedPublisher = new PublisherConfig();
        updatedPublisher.setId(existingPublisher.getId());
        updatedPublisher.setType("api");
        updatedPublisher.setOpenApiDocument("{\"openapi\": \"3.1.0\"}");
        updatedPublisher.setRequestMappings("updated -> mapping");
        updatedPublisher.setConfigProperties(Map.of("updated", "config"));
        updatedConfig.setPublisher(updatedPublisher);
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(updatedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNotNull(response.publisher());
        assertEquals("api", response.publisher().type());
        assertEquals("{\"openapi\": \"3.1.0\"}", response.publisher().openApiDocument());
        
        verify(fileValidationService).validateOpenApiDocument("{\"openapi\": \"3.1.0\"}");
        verify(fileValidationService).validateRequestMappings("updated -> mapping");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithNullPublisher_RemovesExistingPublisher() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                null
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration existingConfig = createTestConfiguration();
        PublisherConfig existingPublisher = new PublisherConfig();
        existingPublisher.setId(UUID.randomUUID());
        existingPublisher.setType("api");
        existingConfig.setPublisher(existingPublisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(existingConfig));
        
        AppConfiguration updatedConfig = createTestConfiguration();
        updatedConfig.setPublisher(null);
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(updatedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNull(response.publisher());
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithWarehousePublisher_UpdatesWarehousePublisher() {
        // Arrange
        com.workflowsaas.dto.request.WarehousePublisherRequest warehouseRequest = 
            new com.workflowsaas.dto.request.WarehousePublisherRequest(
                "{\"type\": \"record\", \"name\": \"Updated\"}",
                "updated -> mapping"
            );
        
        com.workflowsaas.dto.request.PublisherConfigRequest publisherRequest = 
            new com.workflowsaas.dto.request.PublisherConfigRequest(
                "eeh",
                null,
                null,
                null,
                warehouseRequest
            );
        
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                publisherRequest
            );
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration existingConfig = createTestConfiguration();
        PublisherConfig existingPublisher = new PublisherConfig();
        existingPublisher.setId(UUID.randomUUID());
        existingPublisher.setType("eeh");
        
        WarehousePublisherConfig existingWarehouse = new WarehousePublisherConfig();
        existingWarehouse.setId(UUID.randomUUID());
        existingWarehouse.setAvroSchema("{\"type\": \"record\", \"name\": \"Old\"}");
        existingPublisher.setWarehousePublisher(existingWarehouse);
        
        existingConfig.setPublisher(existingPublisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(existingConfig));
        
        AppConfiguration updatedConfig = createTestConfiguration();
        PublisherConfig updatedPublisher = new PublisherConfig();
        updatedPublisher.setId(existingPublisher.getId());
        updatedPublisher.setType("eeh");
        
        WarehousePublisherConfig updatedWarehouse = new WarehousePublisherConfig();
        updatedWarehouse.setId(existingWarehouse.getId());
        updatedWarehouse.setAvroSchema("{\"type\": \"record\", \"name\": \"Updated\"}");
        updatedWarehouse.setMappings("updated -> mapping");
        updatedPublisher.setWarehousePublisher(updatedWarehouse);
        
        updatedConfig.setPublisher(updatedPublisher);
        
        when(appConfigRepository.save(any(AppConfiguration.class)))
                .thenReturn(updatedConfig);
        
        // Act
        AppConfigResponse response = appConfigService.updateConfig(appId, request);
        
        // Assert
        assertNotNull(response.publisher());
        assertNotNull(response.publisher().warehousePublisher());
        assertEquals("{\"type\": \"record\", \"name\": \"Updated\"}", 
                response.publisher().warehousePublisher().avroSchema());
        
        verify(fileValidationService).validateAvroSchema("{\"type\": \"record\", \"name\": \"Updated\"}");
        verify(fileValidationService).validateRequestMappings("updated -> mapping");
        verify(appConfigRepository).save(any(AppConfiguration.class));
    }
    
    @Test
    void updateConfig_WithNonExistentApp_ThrowsUnauthorizedAccessException() {
        // Arrange
        com.workflowsaas.dto.request.CreateAppConfigRequest request = 
            new com.workflowsaas.dto.request.CreateAppConfigRequest(
                "api",
                null,
                null,
                null,
                null
            );
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(com.workflowsaas.exception.UnauthorizedAccessException.class,
                () -> appConfigService.updateConfig(appId, request));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository, never()).save(any());
    }
    
    @Test
    void deleteEnrichmentApi_WithValidIds_DeletesEnrichmentApi() {
        // Arrange
        UUID enrichmentApiId = UUID.randomUUID();
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        EnrichmentApiConfig enrichmentApi = new EnrichmentApiConfig();
        enrichmentApi.setId(enrichmentApiId);
        enrichmentApi.setTenantId(tenantId);
        enrichmentApi.setAppConfiguration(config);
        config.setEnrichmentApis(new ArrayList<>(List.of(enrichmentApi)));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        appConfigService.deleteEnrichmentApi(appId, enrichmentApiId);
        
        // Assert
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(enrichmentApiConfigRepository).deleteByIdAndTenantId(enrichmentApiId, tenantId);
    }
    
    @Test
    void deleteEnrichmentApi_WithNonExistentApp_ThrowsUnauthorizedAccessException() {
        // Arrange
        UUID enrichmentApiId = UUID.randomUUID();
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(com.workflowsaas.exception.UnauthorizedAccessException.class,
                () -> appConfigService.deleteEnrichmentApi(appId, enrichmentApiId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(enrichmentApiConfigRepository, never()).deleteByIdAndTenantId(any(), any());
    }
    
    @Test
    void deleteEnrichmentApi_WithNonExistentConfiguration_ThrowsConfigurationNotFoundException() {
        // Arrange
        UUID enrichmentApiId = UUID.randomUUID();
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ConfigurationNotFoundException.class,
                () -> appConfigService.deleteEnrichmentApi(appId, enrichmentApiId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(enrichmentApiConfigRepository, never()).deleteByIdAndTenantId(any(), any());
    }
    
    @Test
    void deleteEnrichmentApi_WithEnrichmentApiNotBelongingToApp_ThrowsUnauthorizedAccessException() {
        // Arrange
        UUID enrichmentApiId = UUID.randomUUID();
        UUID differentEnrichmentApiId = UUID.randomUUID();
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        EnrichmentApiConfig enrichmentApi = new EnrichmentApiConfig();
        enrichmentApi.setId(differentEnrichmentApiId); // Different ID
        enrichmentApi.setTenantId(tenantId);
        enrichmentApi.setAppConfiguration(config);
        config.setEnrichmentApis(new ArrayList<>(List.of(enrichmentApi)));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act & Assert
        assertThrows(com.workflowsaas.exception.UnauthorizedAccessException.class,
                () -> appConfigService.deleteEnrichmentApi(appId, enrichmentApiId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(enrichmentApiConfigRepository, never()).deleteByIdAndTenantId(any(), any());
    }
    
    @Test
    void deleteEnrichmentApi_UsesTenantIdFromContext() {
        // Arrange
        UUID enrichmentApiId = UUID.randomUUID();
        
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        EnrichmentApiConfig enrichmentApi = new EnrichmentApiConfig();
        enrichmentApi.setId(enrichmentApiId);
        enrichmentApi.setTenantId(tenantId);
        enrichmentApi.setAppConfiguration(config);
        config.setEnrichmentApis(new ArrayList<>(List.of(enrichmentApi)));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        appConfigService.deleteEnrichmentApi(appId, enrichmentApiId);
        
        // Assert - verify tenant ID from context was used
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(enrichmentApiConfigRepository).deleteByIdAndTenantId(enrichmentApiId, tenantId);
    }
    
    @Test
    void deletePublisher_WithValidAppId_DeletesPublisher() {
        // Arrange
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        PublisherConfig publisher = new PublisherConfig();
        publisher.setId(UUID.randomUUID());
        publisher.setTenantId(tenantId);
        publisher.setType("api");
        publisher.setAppConfiguration(config);
        config.setPublisher(publisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        appConfigService.deletePublisher(appId);
        
        // Assert
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(publisherConfigRepository).deleteByAppConfigurationIdAndTenantId(config.getId(), tenantId);
    }
    
    @Test
    void deletePublisher_WithNonExistentApp_ThrowsUnauthorizedAccessException() {
        // Arrange
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(com.workflowsaas.exception.UnauthorizedAccessException.class,
                () -> appConfigService.deletePublisher(appId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(publisherConfigRepository, never()).deleteByAppConfigurationIdAndTenantId(any(), any());
    }
    
    @Test
    void deletePublisher_WithNonExistentConfiguration_ThrowsConfigurationNotFoundException() {
        // Arrange
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ConfigurationNotFoundException.class,
                () -> appConfigService.deletePublisher(appId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(publisherConfigRepository, never()).deleteByAppConfigurationIdAndTenantId(any(), any());
    }
    
    @Test
    void deletePublisher_WithNoPublisher_ThrowsConfigurationNotFoundException() {
        // Arrange
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        config.setPublisher(null); // No publisher
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act & Assert
        assertThrows(ConfigurationNotFoundException.class,
                () -> appConfigService.deletePublisher(appId));
        
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(publisherConfigRepository, never()).deleteByAppConfigurationIdAndTenantId(any(), any());
    }
    
    @Test
    void deletePublisher_UsesTenantIdFromContext() {
        // Arrange
        App app = new App();
        app.setId(appId);
        app.setTenantId(tenantId);
        
        when(appRepository.findByIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(app));
        
        AppConfiguration config = createTestConfiguration();
        PublisherConfig publisher = new PublisherConfig();
        publisher.setId(UUID.randomUUID());
        publisher.setTenantId(tenantId);
        publisher.setType("api");
        publisher.setAppConfiguration(config);
        config.setPublisher(publisher);
        
        when(appConfigRepository.findByAppIdAndTenantId(appId, tenantId))
                .thenReturn(Optional.of(config));
        
        // Act
        appConfigService.deletePublisher(appId);
        
        // Assert - verify tenant ID from context was used
        verify(appRepository).findByIdAndTenantId(appId, tenantId);
        verify(appConfigRepository).findByAppIdAndTenantId(appId, tenantId);
        verify(publisherConfigRepository).deleteByAppConfigurationIdAndTenantId(config.getId(), tenantId);
    }
}
