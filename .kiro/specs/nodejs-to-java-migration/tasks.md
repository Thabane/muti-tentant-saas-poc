# Implementation Plan: Node.js to Java 21 Spring Boot Migration

## Overview

This implementation plan guides the migration of a multi-tenant workflow SaaS platform from Node.js/Express to Java 21 with Spring Boot 3.x. The migration maintains complete API compatibility, preserves the existing database schema, and ensures backward compatibility with the Node.js backend during the transition period.

## Tasks

- [ ] 1. Initialize Spring Boot project structure and dependencies
  - Create Maven project with Spring Boot 3.2.x parent
  - Add dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL driver, jjwt, Lombok, jqwik
  - Configure application.properties with database URL, JWT secret, Camunda URL, and port
  - Set up package structure: config, controller, service, repository, entity, dto, security, exception
  - Create main Application class with @SpringBootApplication
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8, 11.9, 11.10, 11.11_

- [ ] 2. Implement domain entities and database layer
  - [ ] 2.1 Create JPA entities for all tables
    - Create Tenant entity with UUID id, BCrypt password hash, JSONB features, timestamps
    - Create Workflow entity with tenant_id FK, version, BPMN/DMN XML text fields, unique constraint
    - Create Deployment entity with tenant_id and workflow_id FKs, JSONB metadata
    - Create WorkflowExecution entity with tenant_id and workflow_id FKs, JSONB input/output data
    - Configure @PrePersist and @PreUpdate for automatic timestamp management
    - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

  - [ ]* 2.2 Write property test for entity timestamp management
    - **Property 11: Workflow Updates Modify Timestamps**
    - **Validates: Requirements 3.4**

  - [ ] 2.3 Create Spring Data JPA repositories
    - Create TenantRepository with findByEmail, findBySlug, exists methods
    - Create WorkflowRepository with findByTenantId, findByIdAndTenantId methods
    - Create DeploymentRepository with findByTenantId, findByTenantIdAndEnvironment methods
    - Create WorkflowExecutionRepository with findByTenantId methods
    - _Requirements: 3.2, 3.3, 4.4_

  - [ ]* 2.4 Write property test for foreign key cascade deletes
    - **Property 23: Foreign Key Cascade Deletes**
    - **Validates: Requirements 6.8**

- [ ] 3. Implement JWT authentication and security
  - [ ] 3.1 Create JWT token provider
    - Implement JwtTokenProvider with generateToken method (7-day expiration, HS256 algorithm)
    - Implement validateToken method to verify signature and expiration
    - Implement extractTenantId and extractEmail methods
    - Use same JWT secret as Node.js backend for compatibility
    - _Requirements: 2.1, 2.2, 15.2_

  - [ ]* 3.2 Write property test for JWT token structure
    - **Property 2: Authentication Returns Valid Tokens**
    - **Validates: Requirements 1.2, 2.1**

  - [ ]* 3.3 Write property test for JWT interoperability
    - **Property 31: JWT Interoperability**
    - **Validates: Requirements 15.2, 15.3**

  - [ ] 3.4 Create JWT authentication filter
    - Implement JwtAuthenticationFilter extending OncePerRequestFilter
    - Extract Bearer token from Authorization header
    - Validate token and set Spring Security authentication context
    - Store tenant ID in TenantContextHolder (ThreadLocal)
    - Allow unauthenticated access to /health, /api/tenants/register, /api/tenants/login
    - _Requirements: 2.2, 2.3, 2.4_

  - [ ]* 3.5 Write property test for authentication validation
    - **Property 6: Invalid Authentication is Rejected**
    - **Validates: Requirements 2.3, 2.4**

  - [ ]* 3.6 Write property test for valid authentication
    - **Property 7: Valid Authentication Grants Access**
    - **Validates: Requirements 2.2**

  - [ ] 3.7 Create tenant context holder
    - Implement TenantContextHolder with ThreadLocal storage
    - Provide setTenantId, getTenantId, and clear methods
    - Clear context in filter's finally block
    - _Requirements: 2.5_

  - [ ] 3.8 Configure Spring Security
    - Create SecurityConfig with @EnableWebSecurity
    - Configure JWT filter in security filter chain
    - Disable CSRF for stateless API
    - Configure BCrypt password encoder with strength 10
    - Set up authorization rules for endpoints
    - _Requirements: 1.6, 2.1, 2.2, 2.3, 2.4_

- [ ] 4. Checkpoint - Ensure security tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Implement tenant management service and controller
  - [ ] 5.1 Create DTOs for tenant operations
    - Create RegisterRequest record (name, slug, email, password)
    - Create LoginRequest record (email, password)
    - Create AuthResponse record (tenant, token)
    - Create TenantResponse record (id, name, slug, email, features, onboardingCompleted)
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ] 5.2 Implement TenantService
    - Implement register method: validate uniqueness, hash password with BCrypt, save tenant, generate JWT
    - Implement login method: find by email, validate password, generate JWT
    - Implement getCurrentTenant method: get tenant from context, return profile without password
    - Implement completeOnboarding method: update onboarding_completed flag
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

  - [ ]* 5.3 Write property test for tenant registration
    - **Property 1: Tenant Registration Creates Valid Records**
    - **Validates: Requirements 1.1, 1.6, 2.1**

  - [ ]* 5.4 Write property test for duplicate tenant rejection
    - **Property 3: Duplicate Tenant Data is Rejected**
    - **Validates: Requirements 1.5**

  - [ ]* 5.5 Write property test for tenant profile security
    - **Property 4: Tenant Profile Excludes Sensitive Data**
    - **Validates: Requirements 1.3**

  - [ ]* 5.6 Write property test for onboarding completion
    - **Property 5: Onboarding Completion Updates State**
    - **Validates: Requirements 1.4**

  - [ ]* 5.7 Write property test for password compatibility
    - **Property 32: Password Hash Compatibility**
    - **Validates: Requirements 15.4**

  - [ ] 5.8 Implement TenantController
    - Create POST /api/tenants/register endpoint
    - Create POST /api/tenants/login endpoint
    - Create GET /api/tenants/me endpoint (authenticated)
    - Create PATCH /api/tenants/onboarding endpoint (authenticated)
    - Return appropriate HTTP status codes (201 for register, 200 for others)
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [ ]* 5.9 Write integration tests for tenant endpoints
    - Test registration with valid data returns 201 and token
    - Test registration with duplicate email returns 400
    - Test login with valid credentials returns 200 and token
    - Test login with invalid credentials returns 401
    - Test /me endpoint without token returns 401
    - Test /me endpoint with valid token returns tenant profile

- [ ] 6. Implement workflow management service and controller
  - [ ] 6.1 Create DTOs for workflow operations
    - Create CreateWorkflowRequest record (name, type, bpmnXml, dmnXml)
    - Create UpdateWorkflowRequest record (name, bpmnXml, dmnXml)
    - Create TestRunRequest record (inputData)
    - Create WorkflowExecutionResult record (executionId, status, input, output, workflow)
    - _Requirements: 3.1, 3.4, 3.7_

  - [ ] 6.2 Implement WorkflowService
    - Implement createWorkflow: set version 1, status "draft", tenant ID from context, save and return
    - Implement getAllWorkflows: query by tenant ID, order by created_at DESC
    - Implement getWorkflowById: query by ID and tenant ID, throw 404 if not found
    - Implement updateWorkflow: validate ownership, update fields, set updated_at timestamp
    - Implement testRun: create execution record, simulate execution, return mock results (no Camunda call)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [ ]* 6.3 Write property test for workflow creation
    - **Property 9: Workflow Creation with Versioning**
    - **Validates: Requirements 3.1**

  - [ ]* 6.4 Write property test for workflow uniqueness
    - **Property 10: Workflow Uniqueness Constraint**
    - **Validates: Requirements 3.6**

  - [ ]* 6.5 Write property test for tenant isolation
    - **Property 8: Tenant Data Isolation**
    - **Validates: Requirements 2.5, 3.2, 3.3, 3.5, 4.4, 5.6**

  - [ ]* 6.6 Write property test for test runs
    - **Property 12: Test Runs Avoid Camunda**
    - **Validates: Requirements 3.7**

  - [ ] 6.7 Implement WorkflowController
    - Create POST /api/workflows endpoint (authenticated)
    - Create GET /api/workflows endpoint (authenticated)
    - Create GET /api/workflows/:id endpoint (authenticated)
    - Create PUT /api/workflows/:id endpoint (authenticated)
    - Create POST /api/workflows/:id/test endpoint (authenticated)
    - Return appropriate HTTP status codes (201 for create, 200 for others, 404 for not found)
    - _Requirements: 7.5, 7.6, 7.7, 7.8, 7.9_

  - [ ]* 6.8 Write integration tests for workflow endpoints
    - Test creating workflow returns 201 with version 1
    - Test getting all workflows returns only tenant's workflows
    - Test getting workflow by ID returns 404 for other tenant's workflow
    - Test updating workflow modifies updated_at timestamp
    - Test test run returns mock results without Camunda call

- [ ] 7. Checkpoint - Ensure workflow tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. Implement Camunda integration service
  - [ ] 8.1 Create Camunda DTOs
    - Create CamundaDeployment record (id, name, deploymentTime, tenantId)
    - Create CamundaVariable record (value, type)
    - Create ProcessInstance record (id, definitionId, businessKey, tenantId, ended, suspended)
    - _Requirements: 5.1, 5.3, 5.4_

  - [ ] 8.2 Implement CamundaService
    - Implement deployBpmn: create multipart form data, include tenant-id, call Camunda REST API
    - Implement ensureHistoryTimeToLive: parse BPMN XML, add historyTimeToLive="180" if missing
    - Implement convertToProcessVariables: map Java types to Camunda types (String, Integer, Double, Boolean, Json)
    - Implement startProcessInstance: call Camunda REST API with tenant-id and typed variables
    - Implement extractProcessKey: parse BPMN XML to extract process definition key
    - Handle Camunda API errors and wrap in CamundaIntegrationException
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 8.3 Write property test for BPMN history time to live
    - **Property 20: BPMN History Time To Live**
    - **Validates: Requirements 5.2**

  - [ ]* 8.4 Write property test for variable type conversion
    - **Property 21: Variable Type Conversion**
    - **Validates: Requirements 5.3, 5.4**

  - [ ]* 8.5 Write property test for Camunda error handling
    - **Property 22: Camunda Error Handling**
    - **Validates: Requirements 5.5**

  - [ ] 8.6 Configure Camunda REST client
    - Create CamundaConfig with RestTemplate bean
    - Configure base URL from CAMUNDA_REST_URL property
    - Set up connection timeout and read timeout
    - _Requirements: 8.4_

- [ ] 9. Implement deployment management service and controller
  - [ ] 9.1 Create DTOs for deployment operations
    - Create DeploymentRequest record (workflowId, environment, rolloutPercentage)
    - Create PromoteRequest record (targetEnvironment, rolloutPercentage)
    - Create RolloutRequest record (percentage)
    - Create ExecuteRequest record (inputData)
    - _Requirements: 4.1, 4.5, 4.7, 4.8_

  - [ ] 9.2 Implement DeploymentService
    - Implement deploy: validate workflow exists, generate deployment key, call Camunda for non-test, save deployment
    - Implement getDeployments: query by tenant ID, optionally filter by environment
    - Implement promote: validate environment order (test → non-prod → production), create new deployment, mark source as promoted
    - Implement updateRollout: validate percentage 0-100, update deployment record
    - Implement executeWorkflow: validate deployment exists, extract process key, start Camunda instance, record execution
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_

  - [ ]* 9.3 Write property test for deployment creation
    - **Property 13: Deployment Creation with Rollout**
    - **Validates: Requirements 4.1**

  - [ ]* 9.4 Write property test for non-test deployments
    - **Property 14: Non-Test Deployments Call Camunda**
    - **Validates: Requirements 4.2, 5.1**

  - [ ]* 9.5 Write property test for test deployments
    - **Property 15: Test Deployments Skip Camunda**
    - **Validates: Requirements 4.3**

  - [ ]* 9.6 Write property test for deployment promotion validation
    - **Property 16: Deployment Promotion Validation**
    - **Validates: Requirements 4.5, 4.6**

  - [ ]* 9.7 Write property test for deployment promotion
    - **Property 17: Deployment Promotion Creates New Deployment**
    - **Validates: Requirements 4.5**

  - [ ]* 9.8 Write property test for rollout updates
    - **Property 18: Rollout Percentage Updates**
    - **Validates: Requirements 4.7**

  - [ ]* 9.9 Write property test for workflow execution
    - **Property 19: Workflow Execution Starts Process**
    - **Validates: Requirements 4.8**

  - [ ] 9.10 Implement DeploymentController
    - Create POST /api/deployments endpoint (authenticated)
    - Create GET /api/deployments endpoint with optional environment query param (authenticated)
    - Create POST /api/deployments/:id/promote endpoint (authenticated)
    - Create POST /api/deployments/:id/rollout endpoint (authenticated)
    - Create POST /api/deployments/:id/execute endpoint (authenticated)
    - Return appropriate HTTP status codes (201 for create, 200 for others)
    - _Requirements: 7.10, 7.11, 7.12, 7.13, 7.14_

  - [ ]* 9.11 Write integration tests for deployment endpoints
    - Test creating deployment returns 201 with deployment key
    - Test non-test deployment calls Camunda (use WireMock)
    - Test test deployment doesn't call Camunda
    - Test promotion to higher environment succeeds
    - Test promotion to lower environment fails with 400
    - Test rollout update persists percentage
    - Test workflow execution starts Camunda process

- [ ] 10. Checkpoint - Ensure deployment tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Implement global error handling and logging
  - [ ] 11.1 Create custom exception classes
    - Create WorkflowSaasException base class with statusCode field
    - Create ResourceNotFoundException (404)
    - Create UnauthorizedException (401)
    - Create BadRequestException (400)
    - Create CamundaIntegrationException (500)
    - _Requirements: 9.1, 9.4, 9.5_

  - [ ] 11.2 Implement GlobalExceptionHandler
    - Create @RestControllerAdvice class
    - Handle WorkflowSaasException: return ErrorResponse with status code
    - Handle DataIntegrityViolationException: return 400 with descriptive message
    - Handle generic Exception: log error, return 500 with generic message
    - Include stack trace only in development mode
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 7.16_

  - [ ]* 11.3 Write property test for error response format
    - **Property 24: Consistent Error Response Format**
    - **Validates: Requirements 7.16, 9.1, 9.2, 9.3, 9.4, 9.5**

  - [ ] 11.4 Configure request logging
    - Create LoggingFilter to log all requests (method, path, status)
    - Configure Logback to log errors with full stack traces
    - Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
    - _Requirements: 9.6, 9.7, 9.8_

  - [ ]* 11.5 Write property test for request logging
    - **Property 26: Comprehensive Request Logging**
    - **Validates: Requirements 9.6, 9.7, 9.8**

- [ ] 12. Implement CORS and security configuration
  - [ ] 12.1 Configure CORS
    - Create CorsConfig with @Configuration
    - Allow all origins in development mode
    - Allow configured origins in production mode
    - Allow all HTTP methods and headers
    - _Requirements: 10.1, 10.2_

  - [ ]* 12.2 Write property test for CORS configuration
    - **Property 27: CORS Configuration by Environment**
    - **Validates: Requirements 10.1, 10.2**

  - [ ] 12.3 Configure request size limits and security headers
    - Set max request size to 10MB in application.properties
    - Configure security headers: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection
    - Configure HSTS for production with SSL
    - _Requirements: 10.3, 10.4, 10.5_

  - [ ]* 12.4 Write property test for request size limits
    - **Property 28: Request Size Limits**
    - **Validates: Requirements 10.3**

  - [ ]* 12.5 Write property test for security headers
    - **Property 29: Security Headers Present**
    - **Validates: Requirements 10.4**

- [ ] 13. Implement health endpoint and configuration management
  - [ ] 13.1 Create HealthController
    - Implement GET /health endpoint (no authentication required)
    - Return JSON with status "healthy" and current timestamp
    - _Requirements: 7.15_

  - [ ] 13.2 Configure application properties
    - Set up application.properties with PORT (default 3000), DATABASE_URL, JWT_SECRET, CAMUNDA_REST_URL
    - Configure Spring profiles for development and production
    - Add validation for required properties (DATABASE_URL, JWT_SECRET)
    - Configure database connection pool (HikariCP)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [ ]* 13.3 Write property test for required configuration validation
    - **Property 25: Required Configuration Validation**
    - **Validates: Requirements 8.6, 8.7**

  - [ ] 13.4 Configure Spring Boot Actuator
    - Add actuator dependency
    - Enable health, metrics, and info endpoints
    - Configure graceful shutdown
    - _Requirements: 14.2, 14.3_

  - [ ] 13.5 Configure database connection retry
    - Implement connection retry logic with exponential backoff
    - Configure max retry attempts and backoff multiplier
    - Log connection attempts and failures
    - _Requirements: 14.7_

  - [ ]* 13.6 Write property test for database connection retry
    - **Property 30: Database Connection Retry**
    - **Validates: Requirements 14.7**

- [ ] 14. Implement backward compatibility features
  - [ ]* 14.1 Write property test for JSON serialization compatibility
    - **Property 33: JSON Serialization Compatibility**
    - **Validates: Requirements 15.1, 15.5**

  - [ ]* 14.2 Write property test for UUID generation compatibility
    - **Property 34: UUID Generation Compatibility**
    - **Validates: Requirements 15.6**

  - [ ]* 14.3 Write property test for timestamp format compatibility
    - **Property 35: Timestamp Format Compatibility**
    - **Validates: Requirements 15.7**

  - [ ]* 14.4 Write compatibility integration tests
    - Test reading data written by Node.js backend
    - Test Node.js backend can read data written by Java
    - Test JWT tokens are interoperable
    - Test password hashes are interoperable

- [ ] 15. Create build configuration and documentation
  - [ ] 15.1 Configure Maven build
    - Set up pom.xml with Spring Boot Maven plugin
    - Configure build to produce executable JAR
    - Add Maven wrapper for consistent builds
    - Configure test execution and coverage reporting
    - _Requirements: 11.1, 11.11_

  - [ ] 15.2 Create application documentation
    - Create README.md with setup instructions
    - Document environment variables and configuration
    - Document API endpoints (reference Node.js backend docs)
    - Document migration process and compatibility notes
    - Create MIGRATION.md with deployment strategy

  - [ ] 15.3 Create Docker configuration
    - Create Dockerfile for containerized deployment
    - Create docker-compose.yml for local development with PostgreSQL and Camunda
    - Document container deployment process

- [ ] 16. Final checkpoint - Run full test suite
  - Ensure all tests pass, ask the user if questions arise.
  - Run all unit tests
  - Run all integration tests
  - Run all property-based tests (100 iterations each)
  - Verify test coverage meets requirements (80%+ business logic)
  - Run compatibility tests against Node.js backend
  - Verify application starts in under 30 seconds

## Notes

- Tasks marked with `*` are optional property-based tests that can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at major milestones
- Property tests validate universal correctness properties across randomized inputs
- Integration tests validate API contracts and database interactions
- Compatibility tests ensure seamless coexistence with Node.js backend during migration
- The migration maintains complete backward compatibility, allowing gradual rollout
