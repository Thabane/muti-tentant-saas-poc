# Requirements Document: Node.js to Java 21 Spring Boot Migration

## Introduction

This document specifies the requirements for migrating an existing multi-tenant workflow SaaS platform from Node.js/Express to Java 21 with Spring Boot 3.x. The migration must maintain all existing functionality, preserve data integrity, ensure backward compatibility with the frontend, and leverage modern Java features while following Spring Boot best practices.

## Glossary

- **Migration_System**: The Java 21 Spring Boot application being created to replace the Node.js backend
- **Legacy_Backend**: The existing Node.js/Express backend being replaced
- **Tenant**: An isolated customer organization with dedicated data and workflows
- **Workflow**: A BPMN or DMN process definition that can be versioned and deployed
- **Deployment**: An instance of a workflow deployed to a specific environment (test, non-prod, production)
- **Camunda_Engine**: Camunda BPM Platform 7 workflow engine for executing BPMN/DMN processes
- **JWT_Token**: JSON Web Token used for authentication and authorization
- **Tenant_Isolation**: Database-level separation ensuring tenants cannot access each other's data
- **Rollout_Percentage**: The percentage of traffic directed to a deployment (0-100%)
- **Process_Instance**: A running execution of a deployed workflow
- **BPMN_XML**: Business Process Model and Notation XML definition
- **DMN_XML**: Decision Model and Notation XML definition

## Requirements

### Requirement 1: Tenant Management

**User Story:** As a tenant administrator, I want to register, authenticate, and manage my organization's account, so that I can access the workflow platform securely.

#### Acceptance Criteria

1. WHEN a new tenant submits registration data (name, slug, email, password), THE Migration_System SHALL create a tenant record with hashed password and return a JWT token
2. WHEN a tenant submits valid login credentials, THE Migration_System SHALL authenticate the credentials and return a JWT token valid for 7 days
3. WHEN an authenticated tenant requests their profile, THE Migration_System SHALL return tenant information (id, name, slug, email, features, onboarding status) excluding the password hash
4. WHEN an authenticated tenant completes onboarding, THE Migration_System SHALL update the onboarding_completed flag to true
5. WHEN a tenant registration includes a duplicate email or slug, THE Migration_System SHALL reject the registration with an appropriate error message
6. THE Migration_System SHALL hash all passwords using BCrypt with a work factor of at least 10

### Requirement 2: JWT Authentication and Authorization

**User Story:** As a system architect, I want secure JWT-based authentication with tenant context, so that all API requests are properly authenticated and isolated by tenant.

#### Acceptance Criteria

1. WHEN a JWT token is issued, THE Migration_System SHALL include tenant ID and email in the token payload with a 7-day expiration
2. WHEN a protected endpoint receives a request with a valid JWT token, THE Migration_System SHALL extract the tenant context and allow the request
3. WHEN a protected endpoint receives a request without a JWT token, THE Migration_System SHALL reject the request with a 401 Unauthorized status
4. WHEN a protected endpoint receives a request with an invalid or expired JWT token, THE Migration_System SHALL reject the request with a 401 Unauthorized status
5. WHEN a tenant-isolated endpoint is accessed, THE Migration_System SHALL enforce that all database queries include the authenticated tenant's ID
6. THE Migration_System SHALL use the same JWT secret as the Legacy_Backend for backward compatibility during migration

### Requirement 3: Workflow Management

**User Story:** As a workflow designer, I want to create, read, update, and manage BPMN/DMN workflows with versioning, so that I can design and iterate on business processes.

#### Acceptance Criteria

1. WHEN an authenticated tenant creates a workflow with name, type, and XML content, THE Migration_System SHALL store the workflow with version 1 and return the created workflow
2. WHEN an authenticated tenant requests all workflows, THE Migration_System SHALL return only workflows belonging to that tenant, ordered by creation date descending
3. WHEN an authenticated tenant requests a specific workflow by ID, THE Migration_System SHALL return the workflow only if it belongs to that tenant
4. WHEN an authenticated tenant updates a workflow's name or XML content, THE Migration_System SHALL update the workflow and set the updated_at timestamp
5. WHEN an authenticated tenant attempts to access another tenant's workflow, THE Migration_System SHALL return a 404 Not Found error
6. WHEN a workflow is created, THE Migration_System SHALL enforce uniqueness on the combination of tenant_id, name, and version
7. WHEN a workflow test run is requested with input data, THE Migration_System SHALL simulate execution and return mock results without calling Camunda

### Requirement 4: Deployment Management

**User Story:** As a DevOps engineer, I want to deploy workflows to different environments with selective rollouts, so that I can safely promote workflows from test to production.

#### Acceptance Criteria

1. WHEN an authenticated tenant deploys a workflow to an environment, THE Migration_System SHALL create a deployment record with the specified rollout percentage
2. WHEN a workflow is deployed to a non-test environment with BPMN XML, THE Migration_System SHALL deploy the BPMN to Camunda with the tenant ID for isolation
3. WHEN a workflow is deployed to the test environment, THE Migration_System SHALL create the deployment record without calling Camunda
4. WHEN an authenticated tenant requests deployments, THE Migration_System SHALL return only deployments belonging to that tenant, optionally filtered by environment
5. WHEN a deployment is promoted to a higher environment, THE Migration_System SHALL create a new deployment in the target environment and mark the source deployment as promoted
6. WHEN a deployment promotion is requested to a lower or equal environment, THE Migration_System SHALL reject the request with an error
7. WHEN a deployment's rollout percentage is updated, THE Migration_System SHALL update the percentage value (0-100)
8. WHEN a workflow execution is requested for a deployment, THE Migration_System SHALL start a Camunda process instance and record the execution

### Requirement 5: Camunda Integration

**User Story:** As a workflow platform operator, I want seamless integration with Camunda BPM Platform 7, so that workflows can be deployed and executed reliably.

#### Acceptance Criteria

1. WHEN deploying BPMN XML to Camunda, THE Migration_System SHALL include the tenant ID for multi-tenant isolation
2. WHEN BPMN XML lacks a historyTimeToLive attribute, THE Migration_System SHALL add the attribute with a default value of 180 days
3. WHEN starting a Camunda process instance, THE Migration_System SHALL convert input variables to Camunda's typed variable format
4. WHEN converting variables, THE Migration_System SHALL map JavaScript types to Camunda types (String, Integer, Double, Boolean, Json)
5. WHEN a Camunda deployment fails, THE Migration_System SHALL return a descriptive error message to the caller
6. WHEN querying process instances, THE Migration_System SHALL filter by tenant ID to maintain isolation
7. THE Migration_System SHALL support both embedded Camunda engine and REST API client configurations

### Requirement 6: Database Schema and Data Persistence

**User Story:** As a database administrator, I want the Java application to use the same PostgreSQL schema as the Node.js backend, so that data migration is seamless and tenant isolation is preserved.

#### Acceptance Criteria

1. THE Migration_System SHALL use the existing PostgreSQL database schema without modifications
2. WHEN the Migration_System starts, THE Migration_System SHALL connect to PostgreSQL using the DATABASE_URL configuration
3. THE Migration_System SHALL maintain the tenants table with columns: id, name, slug, email, password_hash, features, onboarding_completed, created_at, updated_at
4. THE Migration_System SHALL maintain the workflows table with columns: id, tenant_id, name, type, bpmn_xml, dmn_xml, version, status, created_at, updated_at
5. THE Migration_System SHALL maintain the deployments table with columns: id, tenant_id, workflow_id, environment, deployment_key, status, rollout_percentage, deployed_at, promoted_at, metadata
6. THE Migration_System SHALL maintain the workflow_executions table with columns: id, tenant_id, workflow_id, environment, instance_key, input_data, output_data, status, started_at, completed_at
7. WHEN storing JSON data (features, metadata, input_data, output_data), THE Migration_System SHALL use PostgreSQL JSONB column type
8. THE Migration_System SHALL enforce foreign key constraints and cascade deletes as defined in the existing schema

### Requirement 7: API Endpoint Compatibility

**User Story:** As a frontend developer, I want all existing API endpoints to work identically, so that the frontend requires no changes during the backend migration.

#### Acceptance Criteria

1. THE Migration_System SHALL expose POST /api/tenants/register accepting name, slug, email, password and returning tenant and token
2. THE Migration_System SHALL expose POST /api/tenants/login accepting email, password and returning tenant and token
3. THE Migration_System SHALL expose GET /api/tenants/me requiring authentication and returning tenant profile
4. THE Migration_System SHALL expose PATCH /api/tenants/onboarding requiring authentication and updating onboarding status
5. THE Migration_System SHALL expose POST /api/workflows requiring authentication and creating a workflow
6. THE Migration_System SHALL expose GET /api/workflows requiring authentication and returning all tenant workflows
7. THE Migration_System SHALL expose GET /api/workflows/:id requiring authentication and returning a specific workflow
8. THE Migration_System SHALL expose PUT /api/workflows/:id requiring authentication and updating a workflow
9. THE Migration_System SHALL expose POST /api/workflows/:id/test requiring authentication and executing a test run
10. THE Migration_System SHALL expose POST /api/deployments requiring authentication and creating a deployment
11. THE Migration_System SHALL expose GET /api/deployments requiring authentication and returning deployments with optional environment filter
12. THE Migration_System SHALL expose POST /api/deployments/:id/promote requiring authentication and promoting a deployment
13. THE Migration_System SHALL expose POST /api/deployments/:id/rollout requiring authentication and updating rollout percentage
14. THE Migration_System SHALL expose POST /api/deployments/:id/execute requiring authentication and executing a workflow
15. THE Migration_System SHALL expose GET /health returning status and timestamp without authentication
16. WHEN any endpoint returns an error, THE Migration_System SHALL use the same HTTP status codes and error response format as the Legacy_Backend

### Requirement 8: Configuration Management

**User Story:** As a system operator, I want externalized configuration using environment variables, so that the application can be deployed across different environments without code changes.

#### Acceptance Criteria

1. THE Migration_System SHALL read PORT configuration with a default value of 3000
2. THE Migration_System SHALL read DATABASE_URL configuration for PostgreSQL connection
3. THE Migration_System SHALL read JWT_SECRET configuration for token signing and verification
4. THE Migration_System SHALL read CAMUNDA_REST_URL configuration with a default value of http://localhost:8080/engine-rest
5. THE Migration_System SHALL read NODE_ENV or equivalent configuration to determine development vs production mode
6. WHEN DATABASE_URL is not provided, THE Migration_System SHALL fail to start with a clear error message
7. WHEN JWT_SECRET is not provided, THE Migration_System SHALL fail to start with a clear error message
8. THE Migration_System SHALL support Spring Boot's application.properties and application.yml configuration files

### Requirement 9: Error Handling and Logging

**User Story:** As a system operator, I want comprehensive error handling and logging, so that I can troubleshoot issues and monitor system health.

#### Acceptance Criteria

1. WHEN an unhandled exception occurs, THE Migration_System SHALL return a 500 Internal Server Error with a generic error message
2. WHEN running in development mode, THE Migration_System SHALL include stack traces in error responses
3. WHEN running in production mode, THE Migration_System SHALL exclude stack traces from error responses
4. WHEN a database constraint violation occurs, THE Migration_System SHALL return a 400 Bad Request with a descriptive message
5. WHEN a resource is not found, THE Migration_System SHALL return a 404 Not Found with a descriptive message
6. THE Migration_System SHALL log all incoming requests with method, path, and response status
7. THE Migration_System SHALL log all errors with full stack traces to the application log
8. THE Migration_System SHALL use structured logging with appropriate log levels (DEBUG, INFO, WARN, ERROR)

### Requirement 10: CORS and Security Headers

**User Story:** As a security engineer, I want proper CORS configuration and security headers, so that the API is accessible to the frontend while maintaining security.

#### Acceptance Criteria

1. THE Migration_System SHALL enable CORS for all origins in development mode
2. THE Migration_System SHALL enable CORS for configured origins in production mode
3. THE Migration_System SHALL accept JSON request bodies up to 10MB in size
4. THE Migration_System SHALL set appropriate security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection)
5. WHEN using SSL in production, THE Migration_System SHALL enforce HTTPS and set Strict-Transport-Security headers

### Requirement 11: Build and Dependency Management

**User Story:** As a developer, I want a modern build system with clear dependency management, so that the project is maintainable and follows Java ecosystem best practices.

#### Acceptance Criteria

1. THE Migration_System SHALL use Maven as the build tool
2. THE Migration_System SHALL target Java 21 as the minimum Java version
3. THE Migration_System SHALL use Spring Boot 3.x as the application framework
4. THE Migration_System SHALL include Spring Data JPA for database access
5. THE Migration_System SHALL include Spring Security for authentication and authorization
6. THE Migration_System SHALL include Spring Web for REST API endpoints
7. THE Migration_System SHALL include PostgreSQL JDBC driver for database connectivity
8. THE Migration_System SHALL include a JWT library (such as jjwt) for token handling
9. THE Migration_System SHALL include BCrypt library for password hashing
10. THE Migration_System SHALL include Camunda BPM Platform 7 dependencies (embedded engine or REST client)
11. THE Migration_System SHALL produce an executable JAR file that can be run with java -jar

### Requirement 12: Testing and Quality Assurance

**User Story:** As a quality engineer, I want comprehensive tests to verify the migration maintains functional equivalence, so that we can deploy with confidence.

#### Acceptance Criteria

1. THE Migration_System SHALL include unit tests for all service layer components
2. THE Migration_System SHALL include integration tests for all REST API endpoints
3. THE Migration_System SHALL include tests verifying tenant isolation at the database level
4. THE Migration_System SHALL include tests verifying JWT authentication and authorization
5. THE Migration_System SHALL include tests verifying Camunda integration (deployment and execution)
6. WHEN all tests pass, THE Migration_System SHALL be considered ready for deployment
7. THE Migration_System SHALL achieve at least 80% code coverage for business logic

### Requirement 13: Modern Java Features

**User Story:** As a Java developer, I want to leverage modern Java 21 features, so that the codebase is idiomatic, maintainable, and performant.

#### Acceptance Criteria

1. WHERE appropriate, THE Migration_System SHALL use Java records for immutable data transfer objects
2. WHERE appropriate, THE Migration_System SHALL use sealed classes for restricted type hierarchies
3. WHERE appropriate, THE Migration_System SHALL use pattern matching for instanceof checks
4. WHERE appropriate, THE Migration_System SHALL use text blocks for multi-line strings (SQL, XML)
5. WHERE appropriate, THE Migration_System SHALL use virtual threads for improved concurrency
6. THE Migration_System SHALL use Optional for nullable return values in service methods
7. THE Migration_System SHALL use Stream API for collection processing where it improves readability

### Requirement 14: Deployment and Operations

**User Story:** As a DevOps engineer, I want the Spring Boot application to be easily deployable and operable, so that it can replace the Node.js backend in production.

#### Acceptance Criteria

1. THE Migration_System SHALL start up in under 30 seconds on standard hardware
2. THE Migration_System SHALL expose health check endpoints compatible with container orchestration platforms
3. THE Migration_System SHALL support graceful shutdown to complete in-flight requests
4. THE Migration_System SHALL include actuator endpoints for monitoring (health, metrics, info)
5. THE Migration_System SHALL support running as a standalone JAR or as a container image
6. THE Migration_System SHALL log startup configuration (port, database connection, Camunda URL) without exposing secrets
7. WHEN the database is unavailable at startup, THE Migration_System SHALL retry connection with exponential backoff

### Requirement 15: Data Migration and Backward Compatibility

**User Story:** As a migration engineer, I want the Java application to work with existing data and coexist with the Node.js backend, so that we can perform a gradual migration.

#### Acceptance Criteria

1. THE Migration_System SHALL read and write data in the same format as the Legacy_Backend
2. WHEN the Migration_System generates a JWT token, THE Migration_System SHALL use the same signing algorithm (HS256) and secret as the Legacy_Backend
3. WHEN the Migration_System validates a JWT token, THE Migration_System SHALL accept tokens generated by the Legacy_Backend
4. THE Migration_System SHALL use the same password hashing algorithm (BCrypt) as the Legacy_Backend
5. THE Migration_System SHALL serialize and deserialize JSONB columns identically to the Legacy_Backend
6. THE Migration_System SHALL generate UUIDs for primary keys compatible with PostgreSQL's gen_random_uuid()
7. THE Migration_System SHALL maintain the same timestamp format and timezone handling as the Legacy_Backend
