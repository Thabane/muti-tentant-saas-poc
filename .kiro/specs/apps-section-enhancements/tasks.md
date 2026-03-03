# Implementation Plan: Apps Section Enhancements

## Overview

This implementation plan transforms the "Workflows" section into an "Apps" section with hierarchical resource grouping, automatic API path generation, and API key management. The implementation follows a bottom-up approach: database schema → backend entities → services → controllers → frontend components → migration.

## Tasks

- [x] 1. Create database schema and App entity
  - [x] 1.1 Create database migration for apps table
    - Create migration file with apps table schema
    - Add indexes for tenant_id and api_key_hash
    - Add unique constraint for (tenant_id, name)
    - _Requirements: 7.1_
  
  - [x] 1.2 Create database migration to update workflows table
    - Add app_id column with foreign key to apps table
    - Add parent_workflow_id column with self-referencing foreign key
    - Add api_path and sub_service columns
    - Add indexes for new columns
    - _Requirements: 7.2, 7.3, 3.3_
  
  - [x] 1.3 Create App entity class
    - Define App entity with JPA annotations
    - Include id, tenantId, name, apiKeyHash, timestamps
    - Define one-to-many relationship with Workflow entity
    - _Requirements: 7.1, 7.2_
  
  - [x] 1.4 Update Workflow entity
    - Add app field with many-to-one relationship to App
    - Add parentWorkflowId field for BPMN-DMN hierarchy
    - Add apiPath and subService fields
    - _Requirements: 7.2, 7.3, 3.3, 5.1_
  
  - [ ]* 1.5 Write property test for App-Workflow relationship
    - **Property 10: One-to-Many Relationships**
    - **Validates: Requirements 7.2, 7.3**

- [x] 2. Implement API key generation and validation
  - [x] 2.1 Create ApiKeyService interface and implementation
    - Implement generateApiKey() using SecureRandom
    - Implement hashApiKey() using BCrypt
    - Implement validateApiKey() for hash comparison
    - Format: "app_[32 alphanumeric characters]"
    - _Requirements: 6.1, 6.2, 6.4_
  
  - [ ]* 2.2 Write property test for API key uniqueness
    - **Property 6: API Key Generation and Uniqueness**
    - **Validates: Requirements 6.1, 6.6**
  
  - [ ]* 2.3 Write property test for API key secure storage
    - **Property 7: API Key Secure Storage**
    - **Validates: Requirements 6.2**
  
  - [ ]* 2.4 Write property test for API key validation
    - **Property 8: API Key Validation**
    - **Validates: Requirements 6.4**
  
  - [ ]* 2.5 Write unit tests for API key error cases
    - Test invalid API key format
    - Test API key not found scenario
    - Test regeneration failure after max attempts
    - _Requirements: 6.4_

- [x] 3. Implement API path generation
  - [x] 3.1 Create ApiPathGenerator interface and implementation
    - Implement generateApiPath() with pattern /{tenant-id}/{sub-service}/{service-name}/v1
    - Convert service name to lowercase
    - Replace spaces with hyphens
    - Remove special characters except hyphens
    - Implement isPathUnique() for uniqueness validation
    - Handle conflicts with counter suffix (/v1-2, /v1-3, etc.)
    - _Requirements: 5.1, 5.2, 5.4_
  
  - [ ]* 3.2 Write property test for API path generation pattern
    - **Property 4: API Path Generation Pattern**
    - **Validates: Requirements 5.1, 5.2**
  
  - [ ]* 3.3 Write property test for API path uniqueness
    - **Property 5: API Path Uniqueness**
    - **Validates: Requirements 5.4**
  
  - [ ]* 3.4 Write unit test for specific example
    - Test "Income Service" generates "/tenant-id/sub-service/income-service/v1"
    - _Requirements: 5.5_
  
  - [ ]* 3.5 Write unit tests for edge cases
    - Test service name with only special characters
    - Test service name with multiple consecutive spaces
    - Test duplicate path conflict resolution
    - _Requirements: 5.1, 5.2, 5.4_

- [x] 4. Create App repository and service layer
  - [x] 4.1 Create AppRepository interface
    - Extend JpaRepository<App, UUID>
    - Add findByTenantIdOrderByCreatedAtDesc method
    - Add findByIdAndTenantId method
    - Add findByApiKeyHash method
    - _Requirements: 2.4, 6.4_
  
  - [x] 4.2 Create AppService interface and implementation
    - Implement createApp() with API key generation
    - Implement getAllApps() with tenant filtering
    - Implement getAppById() with tenant validation
    - Implement updateApp() with tenant validation
    - Implement deleteApp() with cascade or prevention logic
    - Implement regenerateApiKey()
    - Implement validateApiKey()
    - _Requirements: 2.2, 6.1, 6.5, 7.5_
  
  - [ ]* 4.3 Write property test for resource association
    - **Property 1: Resource Association with Apps**
    - **Validates: Requirements 2.2**
  
  - [ ]* 4.4 Write property test for API key regeneration
    - **Property 9: API Key Regeneration**
    - **Validates: Requirements 6.5**
  
  - [ ]* 4.5 Write property test for cascade delete behavior
    - **Property 11: Cascade Delete Behavior**
    - **Validates: Requirements 7.5**
  
  - [ ]* 4.6 Write unit tests for error handling
    - Test orphaned resource prevention
    - Test app deletion with existing resources
    - _Requirements: 7.5_

- [x] 5. Update Workflow service for hierarchy support
  - [x] 5.1 Update WorkflowService to support app association
    - Modify createWorkflow() to require appId
    - Modify createWorkflow() to set parentWorkflowId for DMNs
    - Modify createWorkflow() to generate API path for BPMNs
    - Add validation for parent-child relationships
    - Add validation for cross-app reference prevention
    - _Requirements: 2.2, 3.3, 5.1_
  
  - [x] 5.2 Update WorkflowService query methods
    - Modify getAllWorkflows() to return hierarchical structure
    - Group workflows by app
    - Nest DMNs under parent BPMNs
    - _Requirements: 2.3, 3.1_
  
  - [ ]* 5.3 Write property test for hierarchical organization
    - **Property 2: Hierarchical Resource Organization**
    - **Validates: Requirements 2.1, 2.3, 3.1, 3.2, 3.4**
  
  - [ ]* 5.4 Write property test for parent-child integrity
    - **Property 3: Parent-Child Referential Integrity**
    - **Validates: Requirements 3.3, 7.4**
  
  - [ ]* 5.5 Write unit tests for relationship errors
    - Test invalid parent reference
    - Test cross-app reference prevention
    - Test BPMN deletion with DMN children
    - _Requirements: 3.3, 7.4_

- [x] 6. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Create App controller and DTOs
  - [x] 7.1 Create request/response DTOs
    - Create CreateAppRequest record
    - Create UpdateAppRequest record
    - Create AppResponse record with nested WorkflowSummary
    - Create ApiKeyResponse record
    - _Requirements: 6.3_
  
  - [x] 7.2 Create AppController
    - Implement POST /api/apps (createApp)
    - Implement GET /api/apps (getAllApps)
    - Implement GET /api/apps/{id} (getApp)
    - Implement PUT /api/apps/{id} (updateApp)
    - Implement DELETE /api/apps/{id} (deleteApp)
    - Implement POST /api/apps/{id}/regenerate-key (regenerateApiKey)
    - Add tenant context validation
    - _Requirements: 2.3, 6.3, 6.5_
  
  - [ ]* 7.3 Write integration tests for App API endpoints
    - Test full CRUD flow for apps
    - Test API key regeneration flow
    - Test tenant isolation
    - _Requirements: 2.2, 6.1, 6.5_

- [x] 8. Update Workflow controller for new structure
  - [x] 8.1 Update WorkflowController endpoints
    - Modify POST /api/workflows to require appId
    - Modify GET /api/workflows to return hierarchical structure
    - Update response format to include apiPath for BPMNs
    - _Requirements: 2.3, 5.3_
  
  - [ ]* 8.2 Write integration tests for updated workflow endpoints
    - Test workflow creation with app association
    - Test hierarchical response structure
    - Test API path in response
    - _Requirements: 2.2, 2.3, 5.3_

- [x] 9. Implement API key authentication middleware
  - [x] 9.1 Create ApiKeyAuthenticationFilter
    - Extract API key from Authorization header
    - Validate API key using AppService
    - Set tenant context based on validated app
    - Handle authentication errors
    - _Requirements: 6.4_
  
  - [x] 9.2 Configure security for API key authentication
    - Add filter to security chain
    - Configure endpoints that require API key auth
    - _Requirements: 6.4_
  
  - [ ]* 9.3 Write integration tests for API key authentication
    - Test valid API key authentication
    - Test invalid API key rejection
    - Test missing API key handling
    - _Requirements: 6.4_

- [x] 10. Create frontend AppsSection component
  - [x] 10.1 Create AppsSection main component
    - Replace WorkflowsSection with AppsSection
    - Update navigation menu to display "Apps"
    - Update section header to display "Apps"
    - Update breadcrumbs and page titles to use "Apps"
    - Implement loadApps() to fetch apps from API
    - Implement createApp() to create new apps
    - Implement deleteApp() to delete apps
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [ ]* 10.2 Write unit tests for AppsSection
    - Test "Apps" terminology in navigation
    - Test "Apps" terminology in section header
    - Test "Apps" terminology in breadcrumbs
    - _Requirements: 1.1, 1.2, 1.3_

- [x] 11. Create frontend AppTreeView component
  - [x] 11.1 Implement AppTreeView component
    - Create tree structure with App → BPMN → DMN hierarchy
    - Implement node expansion/collapse
    - Render BPMN nodes with "Service" label
    - Render DMN nodes as children of BPMN
    - Display API path for BPMN services
    - Handle BPMNs with no DMNs (no children)
    - _Requirements: 2.1, 3.1, 3.2, 3.4, 3.5, 4.1, 5.3_
  
  - [ ]* 11.2 Write unit tests for AppTreeView
    - Test hierarchical rendering
    - Test node expansion behavior
    - Test "Service" label for BPMNs
    - Test API path display
    - Test BPMN with no DMNs
    - _Requirements: 3.1, 3.2, 3.4, 3.5, 4.1, 5.3_

- [x] 12. Create frontend ApiKeyDisplay component
  - [x] 12.1 Implement ApiKeyDisplay component
    - Display API key with masked/visible toggle
    - Implement copy to clipboard functionality
    - Implement regenerate key functionality with confirmation
    - Show success/error messages
    - _Requirements: 6.3, 6.5_
  
  - [ ]* 12.2 Write unit tests for ApiKeyDisplay
    - Test copy to clipboard
    - Test regenerate key flow
    - Test visibility toggle
    - _Requirements: 6.3, 6.5_

- [x] 13. Checkpoint - Ensure all frontend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Create data migration script
  - [x] 14.1 Implement migration script
    - Create default app for each tenant
    - Associate all existing BPMNs with default app
    - Preserve all parent_workflow_id relationships
    - Generate API keys for all apps
    - Generate API paths for all BPMNs
    - Set sub_service to "default" for existing BPMNs
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
  
  - [ ]* 14.2 Write property test for migration data preservation
    - **Property 12: Migration Data Preservation**
    - **Validates: Requirements 8.3**
  
  - [ ]* 14.3 Write property test for migration completeness
    - **Property 13: Migration Completeness**
    - **Validates: Requirements 8.2, 8.4, 8.5**
  
  - [ ]* 14.4 Write unit tests for migration edge cases
    - Test migration with no existing workflows
    - Test migration with orphaned DMNs
    - Test migration rollback
    - _Requirements: 8.1, 8.2, 8.3_

- [ ] 15. Integration and end-to-end testing
  - [ ] 15.1 Wire all components together
    - Ensure backend APIs are properly connected
    - Ensure frontend components integrate with backend
    - Verify API key authentication flow
    - Verify hierarchical data flow from database to UI
    - _Requirements: 2.1, 2.3, 3.1, 6.4_
  
  - [ ]* 15.2 Write end-to-end tests
    - Test complete app creation flow with BPMN and DMN
    - Test API key authentication for service access
    - Test hierarchical display in UI
    - Test migration and data verification
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 6.4, 8.1_

- [ ] 16. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at backend, frontend, and integration stages
- Property tests validate universal correctness properties with minimum 100 iterations
- Unit tests validate specific examples, edge cases, and error conditions
- Migration script should be tested thoroughly before running on production data
