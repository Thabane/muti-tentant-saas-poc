# Implementation Plan: Multi-Tenant Workflow SaaS Platform

## Overview

This implementation plan tracks the multi-tenant workflow SaaS platform built with Java 17/Spring Boot for the backend, React for the frontend, PostgreSQL for data storage, and Camunda Platform 7 for workflow execution. The system has been migrated from Node.js to Java and includes app-based workflow organization. Tasks marked as complete reflect the current implementation state.

## Tasks

- [x] 1. Set up database schema and tenant isolation infrastructure
  - [x] 1.1 Create Liquibase migrations for core tables
    - Created 000-create-initial-schema.yaml with tenants, workflows, deployments, workflow_executions tables
    - Added indexes for tenant_id columns
    - Configured foreign keys with CASCADE/SET NULL
    - _Requirements: 1.1, 1.2, 13.1_
  
  - [x] 1.2 Create apps table migration
    - Created 001-create-apps-table.yaml
    - Added unique constraint on (tenant_id, name)
    - Added api_key_hash column with unique constraint
    - _Requirements: Apps Section Enhancement_
  
  - [x] 1.3 Update workflows table for app association
    - Created 002-update-workflows-table.yaml
    - Added app_id, parent_workflow_id, api_path, sub_service columns
    - Added indexes for new columns
    - _Requirements: Apps Section Enhancement_
  
  - [x] 1.4 Add tenant features and onboarding
    - Created 003-add-tenant-features.yaml
    - Added features (JSONB) and onboarding_completed columns
    - _Requirements: 2.4, 2.5, 16.1_
  
  - [x] 1.5 Create default tenant for development
    - Created 004-insert-default-tenant.yaml
    - Inserts default tenant with ID 00000000-0000-0000-0000-000000000001
    - Used when security is disabled
    - _Requirements: Development Setup_

- [x] 2. Implement tenant registration and authentication
  - [x] 2.1 Create TenantService with register and login methods
    - Implemented in TenantService.java
    - **Note: Security disabled - passwords stored in plain text**
    - Returns tenant ID as token (no JWT generation)
    - Sets TenantContextHolder for session
    - _Requirements: 1.1, 1.3_
  
  - [ ]* 2.2 Write property test for tenant registration
    - **Property 1: Tenant Registration Creates Unique Isolated Accounts**
    - **Validates: Requirements 1.1, 1.2**
  
  - [ ]* 2.3 Write property test for JWT token generation
    - **Property 2: JWT Tokens Contain Tenant Context**
    - **Note: Skipped - JWT not implemented (security disabled)**
    - **Validates: Requirements 1.3**
  
  - [ ]* 2.4 Write property test for duplicate email rejection
    - **Property 3: Duplicate Email Registration Fails**
    - **Validates: Requirements 1.5**
  
  - [x] 2.5 Create tenant API routes
    - Implemented TenantController.java
    - POST /api/tenants/register
    - POST /api/tenants/login
    - GET /api/tenants/me
    - PATCH /api/tenants/onboarding
    - Validation via Jakarta Bean Validation
    - _Requirements: 1.1, 1.3, 1.5_

- [x] 3. Implement authentication middleware and tenant isolation
  - [x] 3.1 Create tenant context holder
    - Implemented TenantContextHolder.java using ThreadLocal
    - Stores tenant ID for current request thread
    - Used by services to scope queries
    - **Note: No JWT middleware - security disabled**
    - _Requirements: 1.4, 13.3_
  
  - [x] 3.2 Implement tenant isolation in services
    - All services use TenantContextHolder.getTenantId()
    - Falls back to default tenant ID when null (security disabled)
    - All repository queries scoped by tenant_id
    - _Requirements: 13.1_
  
  - [ ]* 3.3 Write property test for unauthenticated request rejection
    - **Property 23: Unauthenticated Requests Rejection**
    - **Note: Skipped - authentication disabled**
    - **Validates: Requirements 13.3**
  
  - [ ]* 3.4 Write property test for tenant isolation in queries
    - **Property 20: Tenant Isolation in API Queries**
    - **Validates: Requirements 1.4, 11.2, 12.1, 13.1**
  
  - [ ]* 3.5 Write property test for cross-tenant access prevention
    - **Property 21: Cross-Tenant Access Prevention**
    - **Validates: Requirements 11.3, 13.4, 13.5**

- [x] 4. Implement tenant onboarding flow
  - [x] 4.1 Add onboarding fields and endpoints
    - Added onboarding_completed field to tenants table
    - Implemented completeOnboarding() in TenantService
    - Created PATCH /api/tenants/onboarding endpoint
    - Created GET /api/tenants/me endpoint
    - _Requirements: 2.4, 2.5_
  
  - [ ]* 4.2 Write property test for onboarding completion
    - **Property 4: Onboarding Completion Updates Tenant State**
    - **Validates: Requirements 2.4, 2.5**
  
  - [x] 4.3 Create Onboarding React component
    - Implemented Onboarding.jsx with multi-step flow
    - 3-step onboarding UI
    - Navigation between steps
    - Calls onboarding completion API
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 5. Checkpoint - Ensure all tests pass
  - Unit tests created for TenantService (11 tests passing)
  - Tests cover registration, login, onboarding with default tenant fallback
  - _Status: ✅ All tests passing_

- [x] 6. Implement workflow CRUD operations
  - [x] 6.1 Create WorkflowService with CRUD methods
    - Implemented WorkflowService.java
    - Methods: createWorkflow, getAllWorkflows, getWorkflowById, updateWorkflow
    - Tenant ID scoping on all queries
    - App association required for all workflows
    - Parent-child validation for BPMN-DMN relationships
    - API path generation for BPMN workflows
    - **Note: Version increment not yet implemented**
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_
  
  - [ ]* 6.2 Write property test for workflow creation with unique IDs
    - **Property 1 (partial): Workflow creation assigns unique identifiers**
    - **Validates: Requirements 11.1**
  
  - [ ]* 6.3 Write property test for workflow ownership verification
    - **Property 22: Workflow Ownership Verification for Mutations**
    - **Validates: Requirements 11.4, 11.5**
  
  - [ ]* 6.4 Write property test for version increment
    - **Property 24: Workflow Version Increment on Update**
    - **Note: Version increment not yet implemented**
    - **Validates: Requirements 14.1**
  
  - [x] 6.5 Create workflow API routes
    - Implemented WorkflowController.java
    - POST /api/workflows (create)
    - GET /api/workflows (list)
    - GET /api/workflows/:id (get)
    - PUT /api/workflows/:id (update)
    - POST /api/workflows/:id/test-run (test execution)
    - **Note: DELETE not yet implemented**
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 7. Integrate bpmn-js for visual workflow designer
  - [x] 7.1 Update WorkflowDesigner component with bpmn-js Modeler
    - Implemented WorkflowDesigner.jsx with BpmnModeler
    - Initializes BpmnModeler with container reference
    - Implements importXML for loading workflows
    - Implements saveXML for persisting workflows
    - Supports both BPMN and DMN (dmn-js)
    - _Requirements: 3.1, 3.3, 3.4, 3.5_
  
  - [ ]* 7.2 Write property test for workflow save/load round trip
    - **Property 5: Workflow Save and Load Round Trip**
    - **Validates: Requirements 3.4, 3.5, 4.4**
  
  - [ ]* 7.3 Write property test for new workflow initial structure
    - **Property 6: New Workflows Have Initial Structure**
    - **Validates: Requirements 3.2, 4.2**
  
  - [x] 7.4 Add workflow name editing and save button functionality
    - Save button connected to backend API
    - Handles save success and error states
    - Shows success/error messages
    - _Requirements: 3.4_

- [x] 8. Implement workflow test execution
  - [x] 8.1 Create test run simulation logic in WorkflowService
    - Implemented testRun() method in WorkflowService.java
    - Returns mock execution results without Camunda interaction
    - Stores execution record in workflow_executions table
    - Sets environment to "test"
    - _Requirements: 5.1, 5.2, 5.4_
  
  - [ ]* 8.2 Write property test for test runs not deploying to Camunda
    - **Property 7: Test Runs Do Not Deploy to Camunda**
    - **Validates: Requirements 5.1, 5.2**
  
  - [ ]* 8.3 Write property test for test run history persistence
    - **Property 8: Test Run History Persistence**
    - **Validates: Requirements 5.4, 5.5**
  
  - [ ]* 8.4 Write property test for invalid input error handling
    - **Property 9: Invalid Input Produces Error Responses**
    - **Validates: Requirements 5.3, 17.1, 17.3**
  
  - [x] 8.5 Add test panel to WorkflowDesigner component
    - Implemented test panel in WorkflowDesigner.jsx
    - JSON input textarea for test data
    - Test run button
    - Displays test results with status and output
    - _Requirements: 5.1, 5.2, 5.3_

- [x] 9. Checkpoint - Ensure all tests pass
  - Unit tests created for WorkflowService (13 tests passing)
  - Tests cover CRUD operations, app association, parent-child validation
  - _Status: ✅ All tests passing_

- [x] 10. Implement Camunda integration service
  - [x] 10.1 Create CamundaService with deployment and execution methods
    - Implemented CamundaService.java
    - Methods: deployBpmn, startProcessInstance, extractProcessKey
    - Uses RestTemplate for Camunda REST API calls
    - Converts variables to Camunda format
    - Error handling and parsing for Camunda responses
    - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_
  
  - [ ]* 10.2 Write property test for variable type conversion
    - **Property 19: Variable Type Conversion to Camunda Format**
    - **Validates: Requirements 9.2, 15.4**
  
  - [ ]* 10.3 Write property test for Camunda deployment format
    - **Property 27: Camunda Deployment Format Compliance**
    - **Validates: Requirements 15.2**
  
  - [ ]* 10.4 Write property test for Camunda error propagation
    - **Property 28: Camunda Error Propagation**
    - **Validates: Requirements 15.5, 17.2**

- [x] 11. Implement deployment management
  - [x] 11.1 Create DeploymentService with create, promote, and execute methods
    - Implemented DeploymentService.java
    - Methods: deploy, getDeployments, promote, updateRollout, executeWorkflow
    - Integrates with CamundaService for non-test environments
    - Environment validation (test → non-prod → production)
    - Stores deployment metadata including Camunda deployment ID
    - _Requirements: 6.2, 6.3, 6.4, 7.1, 7.3, 8.1, 8.2, 8.3, 9.1_
  
  - [ ]* 11.2 Write property test for non-test deployments creating Camunda deployments
    - **Property 10: Non-Test Deployments Create Camunda Deployments**
    - **Validates: Requirements 6.2, 6.4**
  
  - [ ]* 11.3 Write property test for Camunda deployments including tenant ID
    - **Property 11: Camunda Deployments Include Tenant Identifier**
    - **Validates: Requirements 6.3, 9.3, 13.2, 15.3**
  
  - [ ]* 11.4 Write property test for failed Camunda deployments not creating records
    - **Property 12: Failed Camunda Deployments Do Not Create Records**
    - **Validates: Requirements 6.5**
  
  - [ ]* 11.5 Write property test for rollout percentage validation
    - **Property 13: Rollout Percentage Validation and Storage**
    - **Validates: Requirements 7.1, 7.3, 7.4**
  
  - [ ]* 11.6 Write property test for multiple deployments per workflow
    - **Property 14: Multiple Deployments Per Workflow**
    - **Validates: Requirements 7.5**
  
  - [x] 11.7 Create deployment API routes
    - Implemented DeploymentController.java
    - POST /api/deployments (create)
    - GET /api/deployments (list with optional environment filter)
    - POST /api/deployments/:id/promote (promote)
    - POST /api/deployments/:id/rollout (update rollout)
    - POST /api/deployments/:id/execute (execute)
    - _Requirements: 6.2, 7.1, 7.3, 8.1, 9.1_

- [x] 12. Implement environment promotion logic
  - [ ]* 12.1 Write property test for environment promotion
    - **Property 15: Environment Promotion Creates New Deployment**
    - **Validates: Requirements 8.1, 8.2, 8.3**
  
  - [ ]* 12.2 Write property test for production promotion rejection
    - **Property 16: Production Promotion Rejection**
    - **Note: Current implementation allows promotion to higher environments only**
    - **Validates: Requirements 8.4**
  
  - [ ]* 12.3 Write property test for successful promotion deploying to Camunda
    - **Property 17: Successful Promotion Deploys to Camunda**
    - **Validates: Requirements 8.5**

- [x] 13. Implement workflow execution in deployed environments
  - [ ]* 13.1 Write property test for workflow execution creating Camunda instances
    - **Property 18: Workflow Execution Creates Camunda Process Instance**
    - **Validates: Requirements 9.1, 9.4**

- [x] 14. Create Deployments React component
  - [x] 14.1 Implement deployment list view with environment filtering
    - Implemented Deployments.jsx
    - Displays deployments table with workflow name, environment, status, rollout percentage
    - Environment filter dropdown
    - Shows deployment counts by environment
    - _Requirements: 12.1, 12.2, 12.3, 12.4_
  
  - [x] 14.2 Add deployment creation modal
    - Workflow selection dropdown
    - Environment selection
    - Rollout percentage slider
    - _Requirements: 6.2, 7.1_
  
  - [x] 14.3 Add promotion and rollout update functionality
    - Promote button with target environment selection
    - Update rollout button with percentage input
    - _Requirements: 8.1, 8.2, 7.3_
  
  - [x] 14.4 Add workflow execution modal
    - JSON input textarea
    - Execute button
    - Display execution results
    - _Requirements: 9.1_

- [x] 15. Checkpoint - Ensure all tests pass
  - Unit tests created for multiple services
  - Repository tests for WorkflowRepository (15 tests passing)
  - _Status: ✅ All tests passing_

- [x] 16. Implement tenant dashboard
  - [x] 16.1 Create dashboard data aggregation in backend
    - Dashboard.jsx displays workflow and deployment statistics
    - Shows total workflows, active deployments, production deployments
    - **Note: Backend aggregation endpoints not yet implemented**
    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  
  - [ ]* 16.2 Write property test for dashboard data completeness
    - **Property 36: Dashboard Data Completeness**
    - **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 12.2**
  
  - [ ]* 16.3 Write property test for deployment environment filtering
    - **Property 37: Deployment Environment Filtering**
    - **Validates: Requirements 12.3**
  
  - [ ]* 16.4 Write property test for deployment count aggregation
    - **Property 38: Deployment Count Aggregation**
    - **Validates: Requirements 12.4**
  
  - [x] 16.5 Update Dashboard component with statistics cards
    - Displays navigation to Apps and Deployments
    - Shows quick access to workflow designer
    - **Note: Statistics cards not yet fully implemented**
    - _Requirements: 10.1, 10.2_

- [ ] 17. Implement workflow versioning
  - [ ]* 17.1 Write property test for deployment recording workflow version
    - **Property 25: Deployment Records Workflow Version**
    - **Note: Version field exists but increment logic not implemented**
    - **Validates: Requirements 14.3**
  
  - [ ]* 17.2 Write property test for workflow version history
    - **Property 26: Workflow Version History Completeness**
    - **Validates: Requirements 14.4, 14.5**
  
  - [ ] 17.3 Add version history endpoint and UI
    - Create GET /api/workflows/:id/versions endpoint
    - Add version history view in WorkflowDesigner
    - **Status: Not yet implemented**
    - _Requirements: 14.4, 14.5_

- [x] 18. Implement tenant feature configuration
  - [x] 18.1 Add feature configuration to TenantService
    - Added features (JSONB) column to tenants table
    - Stored in tenant entity
    - **Note: Feature flag validation middleware not implemented**
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_
  
  - [ ]* 18.2 Write property test for feature configuration initialization
    - **Property 29: Tenant Feature Configuration Initialization**
    - **Validates: Requirements 16.1, 16.2**
  
  - [ ]* 18.3 Write property test for feature flag enforcement
    - **Property 30: Feature Flag Enforcement**
    - **Validates: Requirements 16.4**

- [ ] 19. Implement execution history tracking
  - [ ] 19.1 Add execution history queries to WorkflowService
    - Create getExecutionHistory method with filtering
    - Support date range and status filters
    - **Status: Not yet implemented**
    - _Requirements: 18.2, 18.5_
  
  - [ ]* 19.2 Write property test for execution history completeness
    - **Property 32: Execution History Completeness**
    - **Validates: Requirements 18.1, 18.2, 18.4**
  
  - [ ]* 19.3 Write property test for execution history filtering
    - **Property 33: Execution History Filtering**
    - **Validates: Requirements 18.5**
  
  - [ ] 19.4 Create execution history UI component
    - Display execution list with input, output, status, timestamps
    - Add date range and status filters
    - **Status: Not yet implemented**
    - _Requirements: 18.2, 18.3, 18.4, 18.5_

- [ ] 20. Implement tenant configuration management
  - [ ] 20.1 Create tenant settings endpoints
    - PATCH /api/tenants/settings (update name, preferences, defaults)
    - GET /api/tenants/settings (retrieve current settings)
    - **Status: Not yet implemented**
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5_
  
  - [ ]* 20.2 Write property test for tenant configuration updates
    - **Property 34: Tenant Configuration Updates**
    - **Validates: Requirements 19.1, 19.2, 19.3, 19.4**
  
  - [ ] 20.3 Create tenant settings UI component
    - Organization name input
    - Notification preferences checkboxes
    - Default rollout percentage slider
    - **Status: Not yet implemented**
    - _Requirements: 19.1, 19.2, 19.3, 19.5_

- [ ] 21. Implement deployment status management
  - [ ]* 21.1 Write property test for deployment status lifecycle
    - **Property 35: Deployment Status Lifecycle**
    - **Validates: Requirements 20.2, 20.3, 20.4**
  
  - [ ] 21.2 Add deployment deactivation endpoint
    - POST /api/deployments/:id/deactivate
    - Update status to 'inactive'
    - **Status: Not yet implemented**
    - _Requirements: 20.4_
  
  - [ ] 21.3 Update Deployments component with status indicators
    - Color-code active, inactive, and failed deployments
    - Add deactivate button for active deployments
    - **Status: Partially implemented**
    - _Requirements: 20.5_

- [x] 22. Implement comprehensive error handling
  - [x] 22.1 Create centralized error handling middleware
    - Implemented GlobalExceptionHandler.java
    - Maps error types to HTTP status codes
    - Formats error responses consistently
    - Logs errors with context
    - Handles validation errors, resource not found, bad requests, unauthorized
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_
  
  - [ ]* 22.2 Write property test for error logging completeness
    - **Property 31: Error Logging Completeness**
    - **Validates: Requirements 17.5**
  
  - [ ] 22.3 Add BPMN XML validation
    - Parse and validate XML structure
    - Check for required BPMN elements
    - Return specific validation errors
    - **Status: Not yet implemented**
    - _Requirements: 17.1_

- [x] 23. Integrate dmn-js for DMN decision tables
  - [x] 23.1 Add DMN support to WorkflowDesigner component
    - Conditionally loads dmn-js Modeler for DMN workflows
    - Implements DMN XML import and export
    - Switches between bpmn-js and dmn-js based on type
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 23.2 Update workflow type selection in UI
    - Type selector on workflow creation (BPMN/DMN)
    - Implemented in AppDetail.jsx
    - Switches modeler based on workflow type
    - _Requirements: 4.1, 4.5_

- [ ] 24. Implement BPMN-DMN integration
  - [ ] 24.1 Add DMN reference extraction to WorkflowService
    - Implement extractDmnReferences method to parse BPMN XML
    - Extract decision keys from Business Rule Tasks
    - Return unique list of DMN references
    - **Status: Not yet implemented**
    - _Requirements: 21.3_
  
  - [ ] 24.2 Add DMN dependency validation to WorkflowService
    - Implement validateDmnDependencies method
    - Check that all referenced DMN workflows exist for tenant
    - Return list of missing references if validation fails
    - **Status: Not yet implemented**
    - _Requirements: 21.5_
  
  - [ ] 24.3 Add DMN usage checking to WorkflowService
    - Implement checkDmnUsage method
    - Find all BPMN workflows that reference a specific DMN workflow
    - Return list of dependent BPMN workflow names
    - **Status: Not yet implemented**
    - _Requirements: 21.8_
  
  - [ ] 24.4 Update workflow delete method with DMN protection
    - Check for dependent BPMN workflows before deleting DMN
    - Reject deletion if dependencies exist
    - Return error with list of dependent workflows
    - **Status: Delete method not yet implemented**
    - _Requirements: 21.8_
  
  - [ ]* 24.5 Write property test for DMN workflow listing
    - **Property 39: DMN Workflow Listing for Business Rule Tasks**
    - **Validates: Requirements 21.6**
  
  - [ ]* 24.6 Write property test for DMN reference storage
    - **Property 40: DMN Reference Storage in BPMN**
    - **Validates: Requirements 21.2**
  
  - [ ]* 24.7 Write property test for DMN dependency extraction
    - **Property 41: DMN Dependency Extraction**
    - **Validates: Requirements 21.3**
  
  - [ ]* 24.8 Write property test for DMN dependency validation
    - **Property 42: DMN Dependency Validation**
    - **Validates: Requirements 21.5**
  
  - [ ]* 24.9 Write property test for DMN deletion protection
    - **Property 44: DMN Deletion Protection**
    - **Validates: Requirements 21.8**

- [ ] 25. Implement multi-resource deployment with DMN dependencies
  - [ ] 25.1 Add deployMultiple method to CamundaService
    - Accept array of resources (BPMN and DMN files)
    - Create multipart form data with all resources
    - Deploy to Camunda in single deployment
    - **Status: Not yet implemented**
    - _Requirements: 21.4_
  
  - [ ] 25.2 Add deployWithDependencies method to DeploymentService
    - Extract DMN references from BPMN workflow
    - Validate DMN dependencies exist
    - Retrieve all referenced DMN workflows
    - Call CamundaService.deployMultiple with all resources
    - Store deployment record with DMN dependency metadata
    - **Status: Not yet implemented**
    - _Requirements: 21.3, 21.4, 21.5_
  
  - [ ] 25.3 Update deployment creation endpoint to use deployWithDependencies
    - Detect if workflow is BPMN type
    - Use deployWithDependencies for BPMN workflows
    - Use existing deploy logic for DMN-only workflows
    - **Status: Not yet implemented**
    - _Requirements: 21.4_
  
  - [ ]* 25.4 Write property test for multi-resource deployment
    - **Property 43: Multi-Resource Deployment with DMN Dependencies**
    - **Validates: Requirements 21.4**

- [ ] 26. Add Business Rule Task configuration UI
  - [ ] 26.1 Add DMN workflow selector to WorkflowDesigner
    - Fetch available DMN workflows when BPMN workflow is loaded
    - Display DMN list in properties panel when Business Rule Task is selected
    - Update BPMN XML with camunda:decisionRef when DMN is selected
    - **Status: Not yet implemented**
    - _Requirements: 21.1, 21.2, 21.6_
  
  - [ ] 26.2 Add properties panel for Business Rule Task configuration
    - Integrate bpmn-js-properties-panel library
    - Add custom property for DMN decision selection
    - Display dropdown of available DMN workflows
    - Extract decision key from selected DMN and set decisionRef attribute
    - **Status: Not yet implemented**
    - _Requirements: 21.1, 21.2_
  
  - [ ] 26.3 Add visual indicator for BPMN workflows with DMN dependencies
    - Display badge or icon on workflows with DMN references
    - Show list of referenced DMN workflows in workflow details
    - **Status: Not yet implemented**
    - _Requirements: 21.3_

- [ ] 27. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 28. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 29. Integration and end-to-end testing
  - [ ]* 29.1 Write integration tests for complete workflows
    - Test: Register → Create Workflow → Deploy → Execute flow
    - Test: Multi-tenant isolation with concurrent requests
    - Test: Environment promotion flow (Test → Non-Prod → Production)
  
  - [ ]* 29.2 Write integration tests for Camunda interaction
    - Test: Real Camunda deployment and process instance creation
    - Test: Variable conversion with actual Camunda API
    - Test: Error handling for Camunda failures
  
  - [ ]* 29.3 Write integration tests for BPMN-DMN integration
    - Test: Deploy BPMN with DMN dependencies to Camunda
    - Test: Execute BPMN process that calls DMN decision
    - Test: Validation failure when DMN dependency is missing
    - Test: DMN deletion prevention when referenced by BPMN
    - **Property 45: Business Rule Task Execution with DMN**
    - **Validates: Requirements 21.7**

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik library
- Unit tests validate specific examples and edge cases
- All property tests should run with minimum 100 iterations
- Camunda Platform 7 should be running via Docker Compose for integration tests

## Current Implementation Status

### ✅ Completed (Core Platform)
- **Database Schema**: All tables created with Liquibase migrations
- **Tenant Management**: Registration, login, onboarding (security disabled for development)
- **Workflow CRUD**: Create, read, update workflows with app association
- **BPMN/DMN Designer**: Visual editors integrated with bpmn-js and dmn-js
- **Test Execution**: Mock test runs without Camunda deployment
- **Camunda Integration**: Service for deploying and executing workflows
- **Deployment Management**: Create, promote, rollout, execute deployments
- **Frontend Pages**: Login, Register, Dashboard, Onboarding, Apps, WorkflowDesigner, Deployments
- **Error Handling**: Global exception handler with consistent error responses
- **Unit Tests**: 60+ tests covering services, repositories, controllers

### ✅ Completed (Apps Section Enhancement)
- **Apps Table**: Database migration with API key support
- **App Entity**: JPA entity with one-to-many relationship to workflows
- **API Key Service**: Secure generation, hashing (SHA-256), validation
- **API Path Generator**: Automatic path generation with uniqueness validation
- **App Service**: Full CRUD operations with hierarchical workflow structure
- **App Controller**: REST API for app management and API key regeneration
- **Apps UI**: React components for app listing, creation, deletion
- **App Detail UI**: Workflow management within apps
- **Parent-Child Relationships**: BPMN workflows can have DMN children
- **Cross-App Prevention**: Validation prevents DMN referencing BPMN in different app

### ⚠️ Security Status
- **Spring Security**: Disabled for development
- **Passwords**: Stored in plain text (not production-ready)
- **JWT**: Not implemented - returns tenant ID as token
- **API Key Auth**: Implemented but not enforced (filter exists but security disabled)
- **Default Tenant**: Falls back to UUID `00000000-0000-0000-0000-000000000001` when no context

### 🚧 Partially Implemented
- **Workflow Versioning**: Version field exists but increment logic not implemented
- **Dashboard Statistics**: UI exists but backend aggregation endpoints missing
- **Deployment Status**: Basic status tracking but deactivation not implemented

### ❌ Not Yet Implemented
- **Workflow Delete**: DELETE endpoint not created
- **Version History**: No endpoint or UI for viewing workflow versions
- **Execution History**: No filtering or UI for viewing execution history
- **Tenant Settings**: No configuration management endpoints or UI
- **BPMN-DMN Integration**: No DMN reference extraction or dependency validation
- **Multi-Resource Deployment**: No support for deploying BPMN with DMN dependencies
- **Business Rule Task UI**: No properties panel for configuring DMN references
- **BPMN XML Validation**: No validation of XML structure before save
- **Property-Based Tests**: No jqwik tests implemented yet
- **Integration Tests**: No end-to-end tests with real Camunda

## Technology Migration Notes

This project was migrated from Node.js/Express to Java 17/Spring Boot:
- **Backend**: Completely rewritten in Java with Spring Boot 3.1.5
- **Database**: Migrated from Sequelize to Liquibase + Spring Data JPA
- **Authentication**: Simplified (security disabled) - needs re-implementation
- **Testing**: Migrated from Jest to JUnit 5 + Mockito
- **Build**: Maven instead of npm for backend

## Next Priority Tasks

1. **Re-enable Security**: Implement JWT authentication and Spring Security configuration
2. **Workflow Delete**: Add DELETE endpoint with cascade/prevention logic
3. **Version Increment**: Implement version increment on workflow updates
4. **Dashboard Aggregation**: Create backend endpoints for statistics
5. **BPMN-DMN Integration**: Implement dependency extraction and validation
6. **Property-Based Tests**: Add jqwik tests for critical properties
7. **Integration Tests**: Add end-to-end tests with Camunda
