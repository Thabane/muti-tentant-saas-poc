# Implementation Plan: App Configuration Management

## Overview

This implementation plan breaks down the app configuration management feature into discrete coding tasks. The feature adds comprehensive configuration capabilities to the Apps section, including data source selection, enrichment API configuration, file uploads (CSV, JSON, Avro), and publisher settings. All configuration data is stored in a normalized relational schema with proper multi-tenant isolation.

The implementation follows Spring Boot best practices with constructor injection, package-private visibility, typed configuration properties, and clear transaction boundaries. Testing includes property-based tests using jqwik to verify universal correctness properties.

## Tasks

- [x] 1. Create database schema migration
  - Create Liquibase changelog for 4 new tables: app_configurations, enrichment_api_configs, publisher_configs, warehouse_publisher_configs
  - Add foreign key constraints with CASCADE DELETE for tenant isolation
  - Add unique constraints for one-to-one relationships
  - Add indexes on tenant_id and foreign key columns
  - _Requirements: 12.1, 12.3_

- [ ] 2. Implement JPA entities and JSONB converter
  - [x] 2.1 Create AppConfiguration entity with relationships
    - Define entity with UUID primary key, tenant_id, app_id, source fields
    - Add parameters and model file fields (filename and content)
    - Define @OneToMany relationship to EnrichmentApiConfig
    - Define @OneToOne relationship to PublisherConfig
    - Add @PrePersist and @PreUpdate for timestamp management
    - _Requirements: 2.3, 4.4, 5.4, 8.6_
  
  - [x] 2.2 Create EnrichmentApiConfig entity
    - Define entity with UUID primary key, tenant_id fields
    - Add openapi_document, request_mappings, config_properties fields
    - Define @ManyToOne relationship to AppConfiguration
    - Add timestamp fields with lifecycle callbacks
    - _Requirements: 3.2, 3.4, 3.6, 3.8_
  
  - [x] 2.3 Create PublisherConfig entity
    - Define entity with UUID primary key, tenant_id, type fields
    - Add openapi_document, request_mappings, config_properties fields
    - Define @OneToOne relationship to AppConfiguration
    - Define @OneToOne relationship to WarehousePublisherConfig
    - Add timestamp fields with lifecycle callbacks
    - _Requirements: 6.3, 6.4, 6.6, 6.8_
  
  - [x] 2.4 Create WarehousePublisherConfig entity
    - Define entity with UUID primary key, tenant_id fields
    - Add avro_schema and mappings fields
    - Define @OneToOne relationship to PublisherConfig
    - Add timestamp fields with lifecycle callbacks
    - _Requirements: 7.3, 7.4, 7.6_
  
  - [x] 2.5 Create JsonbConverter for Map<String, String> to JSONB
    - Implement AttributeConverter interface
    - Use ObjectMapper for JSON serialization/deserialization
    - Handle null and empty map cases
    - Add proper error handling for JSON processing exceptions
    - _Requirements: 3.8, 6.8_

- [ ] 3. Create Spring Data JPA repositories
  - [x] 3.1 Create AppConfigRepository interface
    - Extend JpaRepository<AppConfiguration, UUID>
    - Add method: Optional<AppConfiguration> findByAppIdAndTenantId(UUID appId, UUID tenantId)
    - Add method: void deleteByAppIdAndTenantId(UUID appId, UUID tenantId)
    - _Requirements: 1.4, 11.1, 12.3_
  
  - [x] 3.2 Create EnrichmentApiConfigRepository interface
    - Extend JpaRepository<EnrichmentApiConfig, UUID>
    - Add method: List<EnrichmentApiConfig> findByAppConfigurationIdAndTenantId(UUID appConfigId, UUID tenantId)
    - Add method: void deleteByIdAndTenantId(UUID id, UUID tenantId)
    - _Requirements: 3.2, 3.9, 12.3_
  
  - [x] 3.3 Create PublisherConfigRepository interface
    - Extend JpaRepository<PublisherConfig, UUID>
    - Add method: Optional<PublisherConfig> findByAppConfigurationIdAndTenantId(UUID appConfigId, UUID tenantId)
    - Add method: void deleteByAppConfigurationIdAndTenantId(UUID appConfigId, UUID tenantId)
    - _Requirements: 6.3, 7.2, 12.3_
  
  - [x] 3.4 Create WarehousePublisherConfigRepository interface
    - Extend JpaRepository<WarehousePublisherConfig, UUID>
    - Add method: Optional<WarehousePublisherConfig> findByPublisherConfigIdAndTenantId(UUID publisherConfigId, UUID tenantId)
    - _Requirements: 7.3, 12.3_

- [ ] 4. Implement file validation service
  - [x] 4.1 Create FileValidationService interface and implementation
    - Define interface with validation methods for OpenAPI, Avro, CSV, request mappings
    - Implement FileValidationServiceImpl with package-private visibility
    - Use constructor injection for ObjectMapper dependency
    - _Requirements: 3.4, 4.3, 5.3, 7.4, 9.2, 10.1_
  
  - [x] 4.2 Implement OpenAPI document validation
    - Parse JSON and validate required fields (openapi, info, paths)
    - Validate openapi version format (3.x.x)
    - Throw FileValidationException with specific error messages
    - Include JSON parsing error location in exception message
    - _Requirements: 3.4, 6.4, 9.3_
  
  - [ ]* 4.3 Write property test for OpenAPI validation
    - **Property 5: OpenAPI Document Validation**
    - **Validates: Requirements 3.4, 6.4**
    - Generate random valid and invalid OpenAPI documents
    - Verify valid documents pass validation
    - Verify invalid documents are rejected with specific error messages
  
  - [x] 4.4 Implement Avro schema validation
    - Parse JSON and validate Avro schema structure
    - Validate required fields (name, type, fields for record type)
    - Validate type values against allowed Avro types
    - Throw FileValidationException with specific error messages
    - _Requirements: 7.4, 7.7, 9.3_
  
  - [ ]* 4.5 Write property test for Avro schema validation
    - **Property 12: Avro Schema Validation**
    - **Validates: Requirements 7.4, 7.7**
    - Generate random valid and invalid Avro schemas
    - Verify valid schemas pass validation
    - Verify invalid schemas are rejected with specific error messages
  
  - [x] 4.6 Implement CSV content validation
    - Parse CSV and validate structure (consistent column count per row)
    - Validate proper quote handling
    - Throw FileValidationException with row and column information
    - _Requirements: 4.3, 5.3, 9.4_
  
  - [ ]* 4.7 Write property test for CSV validation
    - **Property 9: CSV File Acceptance and Validation**
    - **Validates: Requirements 4.2, 4.3, 5.2, 5.3**
    - Generate random valid and invalid CSV content
    - Verify valid CSV passes validation
    - Verify invalid CSV is rejected with row/column details
  
  - [x] 4.8 Implement request mappings validation
    - Validate format: "source -> target" with optional pipe separators
    - Validate field names contain only alphanumeric, dots, underscores
    - Trim whitespace from source and target field names
    - Parse pipe-separated mappings into individual entries
    - Throw FileValidationException with format-specific error messages
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
  
  - [ ]* 4.9 Write property tests for request mapping validation
    - **Property 20: Request Mapping Format Validation**
    - **Validates: Requirements 10.1, 10.2**
    - **Property 21: Field Name Character Validation**
    - **Validates: Requirements 10.3**
    - **Property 22: Pipe-Separated Mappings Parsing**
    - **Validates: Requirements 10.4**
    - **Property 23: Whitespace Trimming**
    - **Validates: Requirements 10.5**
    - Generate random request mapping strings with various formats
    - Verify valid mappings pass validation
    - Verify invalid mappings are rejected with specific errors
    - Verify whitespace is trimmed correctly
    - Verify pipe-separated mappings are parsed correctly

- [ ] 5. Create request and response DTOs
  - [x] 5.1 Create request DTOs
    - Create CreateAppConfigRequest record with validation annotations
    - Create EnrichmentApiConfigRequest record
    - Create PublisherConfigRequest record with type validation
    - Create WarehousePublisherRequest record
    - Create ParametersFileRequest and ModelFileRequest records
    - Add @NotNull, @Pattern, @Valid annotations as specified in design
    - _Requirements: 2.3, 2.4, 3.2, 4.2, 5.2, 6.3, 7.2_
  
  - [x] 5.2 Create response DTOs
    - Create AppConfigResponse record
    - Create EnrichmentApiConfigResponse record
    - Create PublisherConfigResponse record
    - Create WarehousePublisherResponse record
    - Create ParametersFileResponse and ModelFileResponse records
    - _Requirements: 1.3, 11.2, 11.3, 11.4, 11.5_

- [ ] 6. Implement app configuration service
  - [x] 6.1 Create AppConfigService interface and implementation
    - Define interface with methods: getConfig, createConfig, updateConfig, deleteEnrichmentApi, deletePublisher
    - Implement AppConfigServiceImpl with package-private visibility
    - Use constructor injection for repositories and FileValidationService
    - _Requirements: 1.4, 8.2, 11.1_
  
  - [x] 6.2 Implement getConfig method with tenant isolation
    - Extract tenant ID from security context
    - Query AppConfigRepository by appId and tenantId
    - Throw ConfigurationNotFoundException if not found
    - Map entity to AppConfigResponse DTO
    - _Requirements: 1.4, 11.1, 12.2, 12.3_
  
  - [ ]* 6.3 Write property test for configuration retrieval
    - **Property 1: Configuration Retrieval**
    - **Validates: Requirements 1.4, 11.1**
    - Generate random app IDs and configurations
    - Verify configurations are retrieved correctly for valid app IDs
  
  - [x] 6.4 Implement createConfig method with validation
    - Extract tenant ID from security context
    - Validate app belongs to tenant
    - Validate source selection is not empty
    - Validate all file uploads using FileValidationService
    - Create AppConfiguration entity with all nested entities
    - Set tenant ID on all entities
    - Save to repository and return response DTO
    - _Requirements: 2.3, 2.4, 8.2, 8.3, 8.4, 12.1, 12.4_
  
  - [ ]* 6.5 Write property tests for configuration creation
    - **Property 2: Source Selection Persistence**
    - **Validates: Requirements 2.3**
    - **Property 3: Source Validation**
    - **Validates: Requirements 2.4**
    - **Property 14: Configuration Validation Before Save**
    - **Validates: Requirements 8.2, 8.3**
    - **Property 15: Configuration Persistence**
    - **Validates: Requirements 8.4**
    - Generate random valid configurations
    - Verify source selection is persisted correctly
    - Verify validation rejects empty source
    - Verify all configuration data is persisted atomically
  
  - [x] 6.6 Implement updateConfig method
    - Extract tenant ID from security context
    - Validate app belongs to tenant
    - Retrieve existing configuration or create new one
    - Validate all file uploads using FileValidationService
    - Update all fields and nested entities
    - Handle orphan removal for deleted enrichment APIs
    - Save to repository and return response DTO
    - _Requirements: 3.9, 4.6, 5.6, 8.2, 8.3, 8.4, 12.4_
  
  - [ ]* 6.7 Write property tests for configuration updates
    - **Property 11: File Replacement**
    - **Validates: Requirements 4.6, 5.6**
    - **Property 16: Timestamp Update on Save**
    - **Validates: Requirements 8.6**
    - Generate random configuration updates
    - Verify file replacement works correctly
    - Verify updated_at timestamp is updated on save
  
  - [x] 6.8 Implement deleteEnrichmentApi method
    - Extract tenant ID from security context
    - Validate enrichment API belongs to tenant's app
    - Delete enrichment API by ID and tenant ID
    - _Requirements: 3.9, 12.4_
  
  - [ ]* 6.9 Write property test for enrichment API operations
    - **Property 4: Multiple Enrichment APIs**
    - **Validates: Requirements 3.2**
    - **Property 8: Enrichment API Removal**
    - **Validates: Requirements 3.9**
    - Generate random number of enrichment API entries
    - Verify all entries are stored correctly
    - Verify removal works correctly
  
  - [x] 6.10 Implement deletePublisher method
    - Extract tenant ID from security context
    - Validate publisher belongs to tenant's app
    - Delete publisher by app configuration ID and tenant ID
    - _Requirements: 12.4_
  
  - [ ]* 6.11 Write property test for tenant isolation
    - **Property 24: Tenant Isolation**
    - **Validates: Requirements 12.1, 12.2, 12.3, 12.4**
    - Generate random tenant IDs and app IDs
    - Verify cross-tenant access is always denied for all operations
    - Verify tenant filtering works correctly

- [x] 7. Checkpoint - Ensure all backend tests pass
  - Run all unit tests and property-based tests
  - Verify all validation logic works correctly
  - Ensure all tests pass, ask the user if questions arise

- [ ] 8. Implement REST API controller
  - [x] 8.1 Create AppConfigController with REST endpoints
    - Create controller with package-private visibility
    - Use constructor injection for AppConfigService
    - Define endpoints: GET, POST, PUT, DELETE for configuration
    - Add @RestController and @RequestMapping("/api/v1/apps/{appId}/config")
    - _Requirements: 1.2, 8.1_
  
  - [x] 8.2 Implement GET /api/v1/apps/{appId}/config endpoint
    - Add @GetMapping with @PathVariable for appId
    - Call AppConfigService.getConfig(appId)
    - Return ResponseEntity with AppConfigResponse
    - Handle ConfigurationNotFoundException and return 404
    - _Requirements: 1.3, 1.4, 11.1_
  
  - [x] 8.3 Implement POST /api/v1/apps/{appId}/config endpoint
    - Add @PostMapping with @PathVariable and @RequestBody @Valid
    - Call AppConfigService.createConfig(appId, request)
    - Return ResponseEntity with 201 Created status
    - _Requirements: 8.1, 8.4, 8.5_
  
  - [x] 8.4 Implement PUT /api/v1/apps/{appId}/config endpoint
    - Add @PutMapping with @PathVariable and @RequestBody @Valid
    - Call AppConfigService.updateConfig(appId, request)
    - Return ResponseEntity with AppConfigResponse
    - _Requirements: 8.1, 8.4, 8.5_
  
  - [x] 8.5 Implement DELETE /api/v1/apps/{appId}/config/enrichment-apis/{enrichmentApiId} endpoint
    - Add @DeleteMapping with @PathVariable for both IDs
    - Call AppConfigService.deleteEnrichmentApi(appId, enrichmentApiId)
    - Return ResponseEntity with 204 No Content
    - _Requirements: 3.9_
  
  - [x] 8.6 Implement DELETE /api/v1/apps/{appId}/config/publisher endpoint
    - Add @DeleteMapping with @PathVariable for appId
    - Call AppConfigService.deletePublisher(appId)
    - Return ResponseEntity with 204 No Content
    - _Requirements: 6.3, 7.2_

- [ ] 9. Implement global exception handler
  - [x] 9.1 Create or update GlobalExceptionHandler
    - Add @RestControllerAdvice annotation
    - Implement handler for MethodArgumentNotValidException
    - Implement handler for FileValidationException
    - Implement handler for ConfigurationNotFoundException
    - Implement handler for UnauthorizedAccessException
    - Return structured error responses with field-specific messages
    - _Requirements: 8.3, 9.1, 9.2, 9.3, 9.4, 9.5_
  
  - [ ]* 9.2 Write unit tests for exception handlers
    - Test validation error response format
    - Test file validation error response format
    - Test not found error response format
    - Test unauthorized access error response format

- [x] 10. Create custom exceptions
  - Create FileValidationException with message and cause
  - Create ConfigurationNotFoundException with app ID
  - Create UnauthorizedAccessException with reason
  - _Requirements: 8.3, 9.2, 12.2_

- [ ]* 11. Write controller unit tests
  - Test all REST endpoints with MockMvc
  - Test request validation with invalid DTOs
  - Test error response formats
  - Test authentication and authorization
  - _Requirements: 1.2, 8.1, 8.3, 9.5_

- [ ]* 12. Write integration tests for end-to-end workflows
  - Test full configuration workflow: create app, create config, update config, retrieve config
  - Test file upload workflow through REST API
  - Test multi-tenant scenarios with multiple tenants
  - Test tenant deletion cascades to configurations
  - Use @SpringBootTest with H2 in-memory database
  - _Requirements: 8.4, 12.1, 12.2, 12.3, 12.4_

- [ ] 13. Checkpoint - Ensure all backend implementation is complete
  - Run all tests including integration tests
  - Verify all REST endpoints work correctly
  - Verify tenant isolation is enforced
  - Ensure all tests pass, ask the user if questions arise

- [ ] 14. Update Apps page with edit button
  - [x] 14.1 Add edit button to AppTreeView component
    - Add edit icon button next to each app in tree view
    - Use React Router Link to navigate to /apps/:appId/config
    - Style button to match existing UI patterns
    - _Requirements: 1.1, 1.2_
  
  - [x] 14.2 Update Apps page routing
    - Add route for /apps/:appId/config to App.jsx
    - Import and render AppDetail component for this route
    - _Requirements: 1.2_

- [ ] 15. Create reusable file upload component
  - [x] 15.1 Create FileUpload component
    - Accept props: label, accept, file, onUpload, error
    - Render file input with label and accept attribute
    - Display current filename if file exists
    - Display error message if error exists
    - Handle file selection and read content as text
    - Call onUpload with {filename, content} object
    - _Requirements: 3.3, 4.2, 5.2, 6.3, 7.3_
  
  - [x] 15.2 Add file upload styling
    - Style file input and labels
    - Style filename display
    - Style error messages in red
    - _Requirements: 9.5_

- [ ] 16. Create configuration form components
  - [x] 16.1 Create SourceSelector component
    - Accept props: value, onChange
    - Render dropdown with options: "", "eeh", "api"
    - Call onChange when selection changes
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 16.2 Create KeyValueEditor component
    - Accept props: label, values, onChange
    - Render list of key-value input pairs
    - Allow adding and removing key-value pairs
    - Call onChange with updated Map when values change
    - _Requirements: 3.8, 6.8_
  
  - [x] 16.3 Create EnrichmentApiEntry component
    - Accept props: api, onUpdate, onRemove
    - Render FileUpload for OpenAPI document
    - Render TextArea for request mappings
    - Render KeyValueEditor for config properties
    - Render remove button
    - Call onUpdate when any field changes
    - _Requirements: 3.3, 3.6, 3.8, 3.9_
  
  - [x] 16.4 Create EnrichmentApiSection component
    - Accept props: enrichmentApis, onAdd, onRemove, onUpdate
    - Render list of EnrichmentApiEntry components
    - Render "Add Enrichment API" button
    - Call appropriate callbacks for add/remove/update
    - _Requirements: 3.2, 3.9_
  
  - [x] 16.5 Create ApiPublisherConfig component
    - Accept props: publisher, onChange
    - Render FileUpload for OpenAPI document
    - Render TextArea for request mappings
    - Render KeyValueEditor for config properties
    - Call onChange when any field changes
    - _Requirements: 6.3, 6.4, 6.6, 6.8_
  
  - [x] 16.6 Create WarehousePublisherConfig component
    - Accept props: warehousePublisher, onChange
    - Render FileUpload for Avro schema
    - Render TextArea for mappings
    - Call onChange when any field changes
    - _Requirements: 7.3, 7.4, 7.6_
  
  - [x] 16.7 Create EehPublisherConfig component
    - Accept props: publisher, onChange
    - Render WarehousePublisherConfig for warehouse publisher
    - Call onChange when warehouse publisher changes
    - _Requirements: 7.2, 7.3_
  
  - [x] 16.8 Create PublisherSection component
    - Accept props: publisher, onChange
    - Render dropdown for publisher type selection
    - Conditionally render ApiPublisherConfig or EehPublisherConfig based on type
    - Call onChange when type or configuration changes
    - _Requirements: 6.2, 7.2_

- [ ] 17. Implement App Details page
  - [x] 17.1 Create AppDetail component structure
    - Use useParams to get appId from URL
    - Set up state for config, loading, errors
    - Create handleSave function to POST/PUT configuration
    - Create handler functions for all form changes
    - _Requirements: 1.2, 1.3, 8.1_
  
  - [x] 17.2 Implement configuration loading
    - Use useEffect to fetch configuration on mount
    - Call GET /api/v1/apps/{appId}/config
    - Set config state with response data
    - Handle 404 by initializing empty config
    - Set loading state appropriately
    - _Requirements: 1.4, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_
  
  - [x] 17.3 Implement configuration save
    - Validate all fields before submitting
    - Call POST or PUT /api/v1/apps/{appId}/config
    - Handle validation errors and display field-specific messages
    - Display success message on successful save
    - Update config state with saved data
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [x] 17.4 Render configuration form
    - Render app name as heading
    - Render SourceSelector component
    - Render EnrichmentApiSection component
    - Render FileUpload for parameters file
    - Render FileUpload for model file
    - Render PublisherSection component
    - Render save button
    - Display loading state while fetching
    - Display error messages inline near relevant fields
    - _Requirements: 1.3, 2.1, 3.1, 4.1, 5.1, 6.1, 8.1, 9.5_
  
  - [x] 17.5 Implement file upload handlers
    - Create handleParametersUpload function
    - Create handleModelUpload function
    - Create handleOpenApiUpload function for enrichment APIs
    - Create handleAvroSchemaUpload function for warehouse publisher
    - Update config state with uploaded file data
    - Handle file validation errors from backend
    - _Requirements: 3.3, 4.2, 5.2, 6.4, 7.4, 9.1, 9.2, 9.3, 9.4_
  
  - [x] 17.6 Implement enrichment API handlers
    - Create handleAddEnrichmentApi function
    - Create handleRemoveEnrichmentApi function
    - Create handleUpdateEnrichmentApi function
    - Update config state appropriately
    - _Requirements: 3.2, 3.9_
  
  - [x] 17.7 Add styling for App Details page
    - Style form layout and sections
    - Style buttons and inputs
    - Style error messages
    - Style success messages
    - Ensure responsive design
    - _Requirements: 1.3, 9.5_

- [x] 18. Update API service
  - Add getAppConfig(appId) function
  - Add createAppConfig(appId, config) function
  - Add updateAppConfig(appId, config) function
  - Add deleteEnrichmentApi(appId, enrichmentApiId) function
  - Add deletePublisher(appId) function
  - _Requirements: 1.4, 8.1, 8.4_

- [x] 19. Final checkpoint - End-to-end testing
  - Test navigation from Apps page to App Details page
  - Test creating new configuration with all fields
  - Test updating existing configuration
  - Test file uploads for all file types
  - Test adding and removing enrichment APIs
  - Test publisher type switching
  - Test validation error display
  - Test multi-tenant isolation in UI
  - Ensure all tests pass, ask the user if questions arise

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik
- Unit tests validate specific examples and edge cases
- Integration tests verify end-to-end workflows
- All backend code follows Spring Boot best practices from guidelines
- Frontend components are reusable and composable
- Multi-tenant isolation is enforced at all layers
