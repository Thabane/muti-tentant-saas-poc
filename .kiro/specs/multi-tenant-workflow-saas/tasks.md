# Implementation Plan: Multi-Tenant Workflow SaaS Platform

## Overview

This implementation plan breaks down the multi-tenant workflow SaaS platform into incremental coding tasks. The system will be built using Node.js/Express for the backend, React for the frontend, PostgreSQL for data storage, and Camunda Platform 7 for workflow execution. Each task builds on previous work, with property-based tests integrated throughout to validate correctness.

## Tasks

- [ ] 1. Set up database schema and tenant isolation infrastructure
  - Create PostgreSQL migration files for tenants, workflows, deployments, and workflow_executions tables
  - Add indexes for tenant_id columns to optimize tenant-scoped queries
  - Implement database connection pooling with tenant context
  - _Requirements: 1.1, 1.2, 13.1_

- [ ] 2. Implement tenant registration and authentication
  - [ ] 2.1 Create TenantService with register and login methods
    - Implement password hashing with bcrypt
    - Generate unique tenant IDs (UUID)
    - Create JWT token generation with tenant context
    - _Requirements: 1.1, 1.3_
  
  - [ ]* 2.2 Write property test for tenant registration
    - **Property 1: Tenant Registration Creates Unique Isolated Accounts**
    - **Validates: Requirements 1.1, 1.2**
  
  - [ ]* 2.3 Write property test for JWT token generation
    - **Property 2: JWT Tokens Contain Tenant Context**
    - **Validates: Requirements 1.3**
  
  - [ ]* 2.4 Write property test for duplicate email rejection
    - **Property 3: Duplicate Email Registration Fails**
    - **Validates: Requirements 1.5**
  
  - [ ] 2.5 Create tenant API routes (POST /api/tenants/register, POST /api/tenants/login)
    - Implement request validation
    - Add error handling for duplicate emails
    - _Requirements: 1.1, 1.3, 1.5_

- [ ] 3. Implement authentication middleware and tenant isolation
  - [ ] 3.1 Create JWT authentication middleware
    - Extract and verify JWT tokens from Authorization header
    - Decode tenant ID and attach to request object
    - _Requirements: 1.4, 13.3_
  
  - [ ] 3.2 Create tenant context middleware
    - Ensure tenant ID is present on all protected routes
    - Add tenant ID to all database queries automatically
    - _Requirements: 13.1_
  
  - [ ]* 3.3 Write property test for unauthenticated request rejection
    - **Property 23: Unauthenticated Requests Rejection**
    - **Validates: Requirements 13.3**
  
  - [ ]* 3.4 Write property test for tenant isolation in queries
    - **Property 20: Tenant Isolation in API Queries**
    - **Validates: Requirements 1.4, 11.2, 12.1, 13.1**
  
  - [ ]* 3.5 Write property test for cross-tenant access prevention
    - **Property 21: Cross-Tenant Access Prevention**
    - **Validates: Requirements 11.3, 13.4, 13.5**

- [ ] 4. Implement tenant onboarding flow
  - [ ] 4.1 Add onboarded field to tenant model and update routes
    - Create PATCH /api/tenants/onboarding endpoint
    - Create GET /api/tenants/me endpoint
    - _Requirements: 2.4, 2.5_
  
  - [ ]* 4.2 Write property test for onboarding completion
    - **Property 4: Onboarding Completion Updates Tenant State**
    - **Validates: Requirements 2.4, 2.5**
  
  - [ ] 4.3 Create Onboarding React component with multi-step flow
    - Implement 3-step onboarding UI
    - Add navigation between steps
    - Call onboarding completion API
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [ ] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement workflow CRUD operations
  - [ ] 6.1 Create WorkflowService with CRUD methods
    - Implement create, getAll, getById, update, delete methods
    - Add tenant ID scoping to all queries
    - Implement version increment logic on updates
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 14.1_
  
  - [ ]* 6.2 Write property test for workflow creation with unique IDs
    - **Property 1 (partial): Workflow creation assigns unique identifiers**
    - **Validates: Requirements 11.1**
  
  - [ ]* 6.3 Write property test for workflow ownership verification
    - **Property 22: Workflow Ownership Verification for Mutations**
    - **Validates: Requirements 11.4, 11.5**
  
  - [ ]* 6.4 Write property test for version increment
    - **Property 24: Workflow Version Increment on Update**
    - **Validates: Requirements 14.1**
  
  - [ ] 6.5 Create workflow API routes
    - POST /api/workflows (create)
    - GET /api/workflows (list)
    - GET /api/workflows/:id (get)
    - PUT /api/workflows/:id (update)
    - DELETE /api/workflows/:id (delete)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 7. Integrate bpmn-js for visual workflow designer
  - [ ] 7.1 Update WorkflowDesigner component with bpmn-js Modeler
    - Initialize BpmnModeler with container reference
    - Implement importXML for loading workflows
    - Implement saveXML for persisting workflows
    - _Requirements: 3.1, 3.3, 3.4, 3.5_
  
  - [ ]* 7.2 Write property test for workflow save/load round trip
    - **Property 5: Workflow Save and Load Round Trip**
    - **Validates: Requirements 3.4, 3.5, 4.4**
  
  - [ ]* 7.3 Write property test for new workflow initial structure
    - **Property 6: New Workflows Have Initial Structure**
    - **Validates: Requirements 3.2, 4.2**
  
  - [ ] 7.4 Add workflow name editing and save button functionality
    - Connect save button to backend API
    - Handle save success and error states
    - _Requirements: 3.4_

- [ ] 8. Implement workflow test execution
  - [ ] 8.1 Create test run simulation logic in WorkflowService
    - Implement testRun method that simulates execution
    - Return mock execution results without Camunda interaction
    - Store execution record in workflow_executions table
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
  
  - [ ] 8.5 Add test panel to WorkflowDesigner component
    - Create test input textarea for JSON
    - Add test run button
    - Display test results with status and output
    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 9. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Implement Camunda integration service
  - [ ] 10.1 Create CamundaService with deployment and execution methods
    - Implement deployProcess method with multipart form data
    - Implement startProcessInstance method with variable conversion
    - Implement convertVariables method for type mapping
    - Add error handling and parsing for Camunda responses
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

- [ ] 11. Implement deployment management
  - [ ] 11.1 Create DeploymentService with create, promote, and execute methods
    - Implement create method with Camunda integration for non-test environments
    - Implement promote method with environment validation
    - Implement updateRollout method
    - Implement execute method with Camunda process instance creation
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
  
  - [ ] 11.7 Create deployment API routes
    - POST /api/deployments (create)
    - GET /api/deployments (list with optional environment filter)
    - POST /api/deployments/:id/promote (promote)
    - POST /api/deployments/:id/rollout (update rollout)
    - POST /api/deployments/:id/execute (execute)
    - _Requirements: 6.2, 7.1, 7.3, 8.1, 9.1_

- [ ] 12. Implement environment promotion logic
  - [ ]* 12.1 Write property test for environment promotion
    - **Property 15: Environment Promotion Creates New Deployment**
    - **Validates: Requirements 8.1, 8.2, 8.3**
  
  - [ ]* 12.2 Write property test for production promotion rejection
    - **Property 16: Production Promotion Rejection**
    - **Validates: Requirements 8.4**
  
  - [ ]* 12.3 Write property test for successful promotion deploying to Camunda
    - **Property 17: Successful Promotion Deploys to Camunda**
    - **Validates: Requirements 8.5**

- [ ] 13. Implement workflow execution in deployed environments
  - [ ]* 13.1 Write property test for workflow execution creating Camunda instances
    - **Property 18: Workflow Execution Creates Camunda Process Instance**
    - **Validates: Requirements 9.1, 9.4**

- [ ] 14. Create Deployments React component
  - [ ] 14.1 Implement deployment list view with environment filtering
    - Display deployments table with workflow name, environment, status, rollout percentage
    - Add environment filter dropdown
    - Show deployment counts by environment
    - _Requirements: 12.1, 12.2, 12.3, 12.4_
  
  - [ ] 14.2 Add deployment creation modal
    - Workflow selection dropdown
    - Environment selection
    - Rollout percentage slider
    - _Requirements: 6.2, 7.1_
  
  - [ ] 14.3 Add promotion and rollout update functionality
    - Promote button with target environment selection
    - Update rollout button with percentage input
    - _Requirements: 8.1, 8.2, 7.3_
  
  - [ ] 14.4 Add workflow execution modal
    - JSON input textarea
    - Execute button
    - Display execution results
    - _Requirements: 9.1_

- [ ] 15. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 16. Implement tenant dashboard
  - [ ] 16.1 Create dashboard data aggregation in backend
    - Add endpoint to return workflow counts, deployment counts by environment
    - Add endpoint to return recent deployments
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
  
  - [ ] 16.5 Update Dashboard component with statistics cards
    - Display total workflows count
    - Display active deployments count
    - Display production deployments count
    - _Requirements: 10.1, 10.2_

- [ ] 17. Implement workflow versioning
  - [ ]* 17.1 Write property test for deployment recording workflow version
    - **Property 25: Deployment Records Workflow Version**
    - **Validates: Requirements 14.3**
  
  - [ ]* 17.2 Write property test for workflow version history
    - **Property 26: Workflow Version History Completeness**
    - **Validates: Requirements 14.4, 14.5**
  
  - [ ] 17.3 Add version history endpoint and UI
    - Create GET /api/workflows/:id/versions endpoint
    - Add version history view in WorkflowDesigner
    - _Requirements: 14.4, 14.5_

- [ ] 18. Implement tenant feature configuration
  - [ ] 18.1 Add feature configuration to TenantService
    - Initialize default features on tenant creation
    - Create updateFeatures method
    - Add feature flag validation middleware
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
    - _Requirements: 18.2, 18.3, 18.4, 18.5_

- [ ] 20. Implement tenant configuration management
  - [ ] 20.1 Create tenant settings endpoints
    - PATCH /api/tenants/settings (update name, preferences, defaults)
    - GET /api/tenants/settings (retrieve current settings)
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5_
  
  - [ ]* 20.2 Write property test for tenant configuration updates
    - **Property 34: Tenant Configuration Updates**
    - **Validates: Requirements 19.1, 19.2, 19.3, 19.4**
  
  - [ ] 20.3 Create tenant settings UI component
    - Organization name input
    - Notification preferences checkboxes
    - Default rollout percentage slider
    - _Requirements: 19.1, 19.2, 19.3, 19.5_

- [ ] 21. Implement deployment status management
  - [ ]* 21.1 Write property test for deployment status lifecycle
    - **Property 35: Deployment Status Lifecycle**
    - **Validates: Requirements 20.2, 20.3, 20.4**
  
  - [ ] 21.2 Add deployment deactivation endpoint
    - POST /api/deployments/:id/deactivate
    - Update status to 'inactive'
    - _Requirements: 20.4_
  
  - [ ] 21.3 Update Deployments component with status indicators
    - Color-code active, inactive, and failed deployments
    - Add deactivate button for active deployments
    - _Requirements: 20.5_

- [ ] 22. Implement comprehensive error handling
  - [ ] 22.1 Create centralized error handling middleware
    - Map error types to HTTP status codes
    - Format error responses consistently
    - Log errors with context
    - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5_
  
  - [ ]* 22.2 Write property test for error logging completeness
    - **Property 31: Error Logging Completeness**
    - **Validates: Requirements 17.5**
  
  - [ ] 22.3 Add BPMN XML validation
    - Parse and validate XML structure
    - Check for required BPMN elements
    - Return specific validation errors
    - _Requirements: 17.1_

- [ ] 23. Integrate dmn-js for DMN decision tables
  - [ ] 23.1 Add DMN support to WorkflowDesigner component
    - Conditionally load dmn-js Modeler for DMN workflows
    - Implement DMN XML import and export
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [ ] 23.2 Update workflow type selection in UI
    - Add BPMN/DMN type selector on workflow creation
    - Switch between bpmn-js and dmn-js based on type
    - _Requirements: 4.1, 4.5_

- [ ] 24. Implement BPMN-DMN integration
  - [ ] 24.1 Add DMN reference extraction to WorkflowService
    - Implement extractDmnReferences method to parse BPMN XML
    - Extract decision keys from Business Rule Tasks
    - Return unique list of DMN references
    - _Requirements: 21.3_
  
  - [ ] 24.2 Add DMN dependency validation to WorkflowService
    - Implement validateDmnDependencies method
    - Check that all referenced DMN workflows exist for tenant
    - Return list of missing references if validation fails
    - _Requirements: 21.5_
  
  - [ ] 24.3 Add DMN usage checking to WorkflowService
    - Implement checkDmnUsage method
    - Find all BPMN workflows that reference a specific DMN workflow
    - Return list of dependent BPMN workflow names
    - _Requirements: 21.8_
  
  - [ ] 24.4 Update workflow delete method with DMN protection
    - Check for dependent BPMN workflows before deleting DMN
    - Reject deletion if dependencies exist
    - Return error with list of dependent workflows
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
    - _Requirements: 21.4_
  
  - [ ] 25.2 Add deployWithDependencies method to DeploymentService
    - Extract DMN references from BPMN workflow
    - Validate DMN dependencies exist
    - Retrieve all referenced DMN workflows
    - Call CamundaService.deployMultiple with all resources
    - Store deployment record with DMN dependency metadata
    - _Requirements: 21.3, 21.4, 21.5_
  
  - [ ] 25.3 Update deployment creation endpoint to use deployWithDependencies
    - Detect if workflow is BPMN type
    - Use deployWithDependencies for BPMN workflows
    - Use existing deploy logic for DMN-only workflows
    - _Requirements: 21.4_
  
  - [ ]* 25.4 Write property test for multi-resource deployment
    - **Property 43: Multi-Resource Deployment with DMN Dependencies**
    - **Validates: Requirements 21.4**

- [ ] 26. Add Business Rule Task configuration UI
  - [ ] 26.1 Add DMN workflow selector to WorkflowDesigner
    - Fetch available DMN workflows when BPMN workflow is loaded
    - Display DMN list in properties panel when Business Rule Task is selected
    - Update BPMN XML with camunda:decisionRef when DMN is selected
    - _Requirements: 21.1, 21.2, 21.6_
  
  - [ ] 26.2 Add properties panel for Business Rule Task configuration
    - Integrate bpmn-js-properties-panel library
    - Add custom property for DMN decision selection
    - Display dropdown of available DMN workflows
    - Extract decision key from selected DMN and set decisionRef attribute
    - _Requirements: 21.1, 21.2_
  
  - [ ] 26.3 Add visual indicator for BPMN workflows with DMN dependencies
    - Display badge or icon on workflows with DMN references
    - Show list of referenced DMN workflows in workflow details
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
- Property tests validate universal correctness properties using fast-check library
- Unit tests validate specific examples and edge cases
- All property tests should run with minimum 100 iterations
- Camunda Platform 7 should be running via Docker Compose for integration tests
