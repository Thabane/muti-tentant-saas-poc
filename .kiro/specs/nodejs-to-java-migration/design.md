# Design Document: Node.js to Java 21 Spring Boot Migration

## Overview

This design document outlines the architecture and implementation approach for migrating a multi-tenant workflow SaaS platform from Node.js/Express to Java 21 with Spring Boot 3.x. The migration will maintain complete functional equivalence while leveraging modern Java features and Spring Boot best practices.

### Migration Strategy

The migration follows a **like-for-like replacement** strategy where:
- All existing API endpoints remain unchanged
- Database schema is preserved without modifications
- JWT tokens are interoperable between old and new systems
- Frontend requires zero changes
- Gradual rollout is possible (both systems can coexist)

### Technology Stack

**Core Framework:**
- Java 21 (LTS)
- Spring Boot 3.2.x
- Spring Web (REST APIs)
- Spring Data JPA (Database access)
- Spring Security (Authentication/Authorization)

**Database:**
- PostgreSQL 14+ with JSONB support
- HikariCP connection pooling

**Workflow Engine:**
- Camunda BPM Platform 7.20+ (REST client mode)

**Build Tool:**
- Maven 3.9+ (chosen for enterprise compatibility and widespread adoption)

**Key Libraries:**
- jjwt (JWT token handling)
- BCrypt (password hashing via Spring Security)
- Jackson (JSON serialization)
- Lombok (boilerplate reduction)
- Logback (logging)


## Architecture

### Layered Architecture

The application follows Spring Boot's standard layered architecture:

```
┌─────────────────────────────────────────┐
│         REST Controllers                │  ← HTTP endpoints
├─────────────────────────────────────────┤
│         Security Filters                │  ← JWT authentication
├─────────────────────────────────────────┤
│         Service Layer                   │  ← Business logic
├─────────────────────────────────────────┤
│         Repository Layer                │  ← Data access (JPA)
├─────────────────────────────────────────┤
│         Domain Models (Entities)        │  ← JPA entities
└─────────────────────────────────────────┘
         ↓                    ↓
   PostgreSQL            Camunda REST API
```

### Package Structure

```
com.workflowsaas
├── config/                    # Spring configuration classes
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   ├── CorsConfig.java
│   └── CamundaConfig.java
├── controller/                # REST controllers
│   ├── TenantController.java
│   ├── WorkflowController.java
│   └── DeploymentController.java
├── service/                   # Business logic
│   ├── TenantService.java
│   ├── WorkflowService.java
│   ├── DeploymentService.java
│   └── CamundaService.java
├── repository/                # JPA repositories
│   ├── TenantRepository.java
│   ├── WorkflowRepository.java
│   ├── DeploymentRepository.java
│   └── WorkflowExecutionRepository.java
├── entity/                    # JPA entities
│   ├── Tenant.java
│   ├── Workflow.java
│   ├── Deployment.java
│   └── WorkflowExecution.java
├── dto/                       # Data transfer objects
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateWorkflowRequest.java
│   │   └── DeploymentRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── TenantResponse.java
│       └── ErrorResponse.java
├── security/                  # Security components
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── TenantContext.java
│   └── TenantContextHolder.java
├── exception/                 # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
└── Application.java           # Main application class
```

### Multi-Tenancy Pattern

**Tenant Context Propagation:**
- JWT token contains tenant ID
- `JwtAuthenticationFilter` extracts tenant ID and stores in `TenantContextHolder` (ThreadLocal)
- All service methods access tenant ID from context
- Repository queries automatically filter by tenant ID

**Database-Level Isolation:**
- Every table has a `tenant_id` foreign key
- JPA queries include `WHERE tenant_id = :tenantId`
- No shared data between tenants
- Foreign key constraints enforce referential integrity per tenant


## Components and Interfaces

### 1. Security Components

#### JwtTokenProvider

Handles JWT token creation and validation.

```java
public class JwtTokenProvider {
    String generateToken(UUID tenantId, String email);
    Claims validateToken(String token);
    UUID extractTenantId(String token);
}
```

**Key Responsibilities:**
- Generate JWT tokens with 7-day expiration
- Sign tokens using HS256 algorithm with configured secret
- Validate token signature and expiration
- Extract claims (tenant ID, email) from token
- Use same secret as Node.js backend for interoperability

#### JwtAuthenticationFilter

Spring Security filter that intercepts requests and validates JWT tokens.

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain);
}
```

**Key Responsibilities:**
- Extract JWT token from Authorization header (Bearer scheme)
- Validate token using JwtTokenProvider
- Set Spring Security authentication context
- Store tenant ID in TenantContextHolder
- Allow unauthenticated access to public endpoints (/health, /api/tenants/register, /api/tenants/login)

#### TenantContextHolder

Thread-local storage for tenant context.

```java
public class TenantContextHolder {
    private static final ThreadLocal<UUID> tenantContext = new ThreadLocal<>();
    
    public static void setTenantId(UUID tenantId);
    public static UUID getTenantId();
    public static void clear();
}
```

**Key Responsibilities:**
- Store tenant ID for current request thread
- Provide access to tenant context throughout request lifecycle
- Clear context after request completion (in filter's finally block)

### 2. Service Layer

#### TenantService

Manages tenant registration, authentication, and profile operations.

```java
@Service
public class TenantService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    TenantResponse getCurrentTenant();
    void completeOnboarding();
}
```

**Key Responsibilities:**
- Hash passwords using BCrypt (strength 10)
- Validate unique email and slug constraints
- Generate JWT tokens after successful registration/login
- Retrieve tenant profile for authenticated user
- Update onboarding status

#### WorkflowService

Manages workflow CRUD operations and test execution.

```java
@Service
public class WorkflowService {
    Workflow createWorkflow(CreateWorkflowRequest request);
    List<Workflow> getAllWorkflows();
    Workflow getWorkflowById(UUID id);
    Workflow updateWorkflow(UUID id, UpdateWorkflowRequest request);
    WorkflowExecutionResult testRun(UUID workflowId, Map<String, Object> inputData);
}
```

**Key Responsibilities:**
- Create workflows with version 1 and draft status
- Enforce tenant isolation in all queries
- Validate workflow ownership before updates
- Simulate workflow execution for test runs (no Camunda call)
- Return mock execution results for test environment

#### DeploymentService

Manages workflow deployments across environments.

```java
@Service
public class DeploymentService {
    Deployment deploy(DeploymentRequest request);
    List<Deployment> getDeployments(String environment);
    Deployment promote(UUID deploymentId, PromoteRequest request);
    Deployment updateRollout(UUID deploymentId, int percentage);
    ProcessInstance executeWorkflow(UUID deploymentId, Map<String, Object> inputData);
}
```

**Key Responsibilities:**
- Create deployment records with unique deployment keys
- Call CamundaService for non-test environments
- Validate environment promotion order (test → non-prod → production)
- Update rollout percentages (0-100)
- Start Camunda process instances for deployed workflows
- Record workflow executions in database

#### CamundaService

Handles all interactions with Camunda BPM Platform 7 REST API.

```java
@Service
public class CamundaService {
    CamundaDeployment deployBpmn(String bpmnXml, String deploymentName, UUID tenantId);
    String ensureHistoryTimeToLive(String bpmnXml);
    ProcessInstance startProcessInstance(String processKey, Map<String, Object> variables, UUID tenantId);
    Map<String, CamundaVariable> convertToProcessVariables(Map<String, Object> data);
    String extractProcessKey(String bpmnXml);
}
```

**Key Responsibilities:**
- Deploy BPMN XML to Camunda via multipart form data
- Add historyTimeToLive attribute if missing (default 180 days)
- Convert Java objects to Camunda typed variables
- Start process instances with tenant ID for isolation
- Extract process definition key from BPMN XML
- Handle Camunda API errors gracefully


### 3. Repository Layer

All repositories extend `JpaRepository` and include tenant isolation.

#### TenantRepository

```java
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByEmail(String email);
    Optional<Tenant> findBySlug(String slug);
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
}
```

#### WorkflowRepository

```java
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
    List<Workflow> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    Optional<Workflow> findByIdAndTenantId(UUID id, UUID tenantId);
    boolean existsByTenantIdAndNameAndVersion(UUID tenantId, String name, Integer version);
}
```

#### DeploymentRepository

```java
@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {
    List<Deployment> findByTenantIdOrderByDeployedAtDesc(UUID tenantId);
    List<Deployment> findByTenantIdAndEnvironmentOrderByDeployedAtDesc(UUID tenantId, String environment);
    Optional<Deployment> findByIdAndTenantId(UUID id, UUID tenantId);
}
```

#### WorkflowExecutionRepository

```java
@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {
    List<WorkflowExecution> findByTenantIdOrderByStartedAtDesc(UUID tenantId);
    List<WorkflowExecution> findByWorkflowIdAndTenantId(UUID workflowId, UUID tenantId);
}
```

### 4. REST Controllers

All controllers use `@RestController` and return appropriate HTTP status codes.

#### TenantController

```java
@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    @PostMapping("/register")
    ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request);
    
    @PostMapping("/login")
    ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request);
    
    @GetMapping("/me")
    ResponseEntity<TenantResponse> getCurrentTenant();
    
    @PatchMapping("/onboarding")
    ResponseEntity<Map<String, Boolean>> completeOnboarding();
}
```

#### WorkflowController

```java
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    @PostMapping
    ResponseEntity<Workflow> createWorkflow(@RequestBody CreateWorkflowRequest request);
    
    @GetMapping
    ResponseEntity<List<Workflow>> getAllWorkflows();
    
    @GetMapping("/{id}")
    ResponseEntity<Workflow> getWorkflow(@PathVariable UUID id);
    
    @PutMapping("/{id}")
    ResponseEntity<Workflow> updateWorkflow(@PathVariable UUID id, @RequestBody UpdateWorkflowRequest request);
    
    @PostMapping("/{id}/test")
    ResponseEntity<WorkflowExecutionResult> testRun(@PathVariable UUID id, @RequestBody TestRunRequest request);
}
```

#### DeploymentController

```java
@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {
    @PostMapping
    ResponseEntity<Deployment> createDeployment(@RequestBody DeploymentRequest request);
    
    @GetMapping
    ResponseEntity<List<Deployment>> getDeployments(@RequestParam(required = false) String environment);
    
    @PostMapping("/{id}/promote")
    ResponseEntity<Deployment> promoteDeployment(@PathVariable UUID id, @RequestBody PromoteRequest request);
    
    @PostMapping("/{id}/rollout")
    ResponseEntity<Deployment> updateRollout(@PathVariable UUID id, @RequestBody RolloutRequest request);
    
    @PostMapping("/{id}/execute")
    ResponseEntity<ProcessInstance> executeWorkflow(@PathVariable UUID id, @RequestBody ExecuteRequest request);
}
```

#### HealthController

```java
@RestController
public class HealthController {
    @GetMapping("/health")
    ResponseEntity<Map<String, Object>> health();
}
```


## Data Models

### JPA Entities

All entities use UUID primary keys and include audit timestamps.

#### Tenant Entity

```java
@Entity
@Table(name = "tenants")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> features = new HashMap<>();
    
    @Column(name = "onboarding_completed")
    private Boolean onboardingCompleted = false;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### Workflow Entity

```java
@Entity
@Table(name = "workflows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "name", "version"}))
public class Workflow {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, length = 50)
    private String type;
    
    @Column(name = "bpmn_xml", columnDefinition = "TEXT")
    private String bpmnXml;
    
    @Column(name = "dmn_xml", columnDefinition = "TEXT")
    private String dmnXml;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column(length = 50)
    private String status = "draft";
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### Deployment Entity

```java
@Entity
@Table(name = "deployments")
public class Deployment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;
    
    @Column(nullable = false, length = 50)
    private String environment;
    
    @Column(name = "deployment_key")
    private String deploymentKey;
    
    @Column(length = 50)
    private String status = "pending";
    
    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage = 0;
    
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;
    
    @Column(name = "promoted_at")
    private LocalDateTime promotedAt;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();
    
    @PrePersist
    protected void onCreate() {
        deployedAt = LocalDateTime.now();
    }
}
```

#### WorkflowExecution Entity

```java
@Entity
@Table(name = "workflow_executions")
public class WorkflowExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    
    @Column(name = "workflow_id", nullable = false)
    private UUID workflowId;
    
    @Column(nullable = false, length = 50)
    private String environment;
    
    @Column(name = "instance_key")
    private String instanceKey;
    
    @Type(JsonBinaryType.class)
    @Column(name = "input_data", columnDefinition = "jsonb")
    private Map<String, Object> inputData;
    
    @Type(JsonBinaryType.class)
    @Column(name = "output_data", columnDefinition = "jsonb")
    private Map<String, Object> outputData;
    
    @Column(length = 50)
    private String status = "running";
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }
}
```

### Data Transfer Objects (DTOs)

DTOs use Java records for immutability and conciseness.

#### Request DTOs

```java
public record RegisterRequest(
    String name,
    String slug,
    String email,
    String password
) {}

public record LoginRequest(
    String email,
    String password
) {}

public record CreateWorkflowRequest(
    String name,
    String type,
    String bpmnXml,
    String dmnXml
) {}

public record UpdateWorkflowRequest(
    String name,
    String bpmnXml,
    String dmnXml
) {}

public record DeploymentRequest(
    UUID workflowId,
    String environment,
    Integer rolloutPercentage
) {}

public record PromoteRequest(
    String targetEnvironment,
    Integer rolloutPercentage
) {}

public record RolloutRequest(
    Integer percentage
) {}

public record TestRunRequest(
    Map<String, Object> inputData
) {}

public record ExecuteRequest(
    Map<String, Object> inputData
) {}
```

#### Response DTOs

```java
public record AuthResponse(
    TenantResponse tenant,
    String token
) {}

public record TenantResponse(
    UUID id,
    String name,
    String slug,
    String email,
    Map<String, Object> features,
    Boolean onboardingCompleted
) {}

public record ErrorResponse(
    String error,
    String stack  // Only in development mode
) {}

public record WorkflowExecutionResult(
    UUID executionId,
    String status,
    Map<String, Object> input,
    Map<String, Object> output,
    WorkflowInfo workflow
) {}

public record WorkflowInfo(
    UUID id,
    String name
) {}
```

### Camunda Data Models

```java
public record CamundaDeployment(
    String id,
    String name,
    String deploymentTime,
    String tenantId
) {}

public record CamundaVariable(
    Object value,
    String type
) {}

public record ProcessInstance(
    String id,
    String definitionId,
    String businessKey,
    String tenantId,
    boolean ended,
    boolean suspended
) {}
```


## Correctness Properties

A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.

### Property 1: Tenant Registration Creates Valid Records

*For any* valid registration data (name, slug, email, password), when a tenant registers, the system should create a database record with a BCrypt-hashed password (work factor >= 10), return a valid JWT token containing the tenant ID and email with 7-day expiration, and the password hash should never appear in the response.

**Validates: Requirements 1.1, 1.6, 2.1**

### Property 2: Authentication Returns Valid Tokens

*For any* registered tenant with valid credentials, logging in should return a JWT token that can be decoded to reveal the tenant ID, email, and an expiration timestamp 7 days in the future.

**Validates: Requirements 1.2, 2.1**

### Property 3: Duplicate Tenant Data is Rejected

*For any* existing tenant, attempting to register a new tenant with the same email or slug should fail with an appropriate error message, and no new database record should be created.

**Validates: Requirements 1.5**

### Property 4: Tenant Profile Excludes Sensitive Data

*For any* authenticated tenant requesting their profile, the response should contain id, name, slug, email, features, and onboarding_completed fields, but should never contain the password_hash field.

**Validates: Requirements 1.3**

### Property 5: Onboarding Completion Updates State

*For any* authenticated tenant, calling the onboarding completion endpoint should result in the onboarding_completed flag being set to true in the database.

**Validates: Requirements 1.4**

### Property 6: Invalid Authentication is Rejected

*For any* protected endpoint, requests without a JWT token or with an invalid/expired JWT token should be rejected with a 401 Unauthorized status.

**Validates: Requirements 2.3, 2.4**

### Property 7: Valid Authentication Grants Access

*For any* protected endpoint and valid JWT token, the request should be allowed (not return 401), and the tenant context should be properly extracted and available throughout the request lifecycle.

**Validates: Requirements 2.2**

### Property 8: Tenant Data Isolation

*For any* two different tenants A and B, tenant A should never be able to access, modify, or view tenant B's workflows, deployments, or executions, regardless of the API endpoint used. All database queries should automatically filter by the authenticated tenant's ID.

**Validates: Requirements 2.5, 3.2, 3.3, 3.5, 4.4, 5.6**

### Property 9: Workflow Creation with Versioning

*For any* authenticated tenant creating a workflow with valid data (name, type, XML), the system should store the workflow with version 1, status "draft", and return the created workflow with all fields populated including timestamps.

**Validates: Requirements 3.1**

### Property 10: Workflow Uniqueness Constraint

*For any* tenant, attempting to create two workflows with the same name and version should fail, with the second creation being rejected due to the uniqueness constraint.

**Validates: Requirements 3.6**

### Property 11: Workflow Updates Modify Timestamps

*For any* workflow update (name or XML content), the updated_at timestamp should be changed to a value greater than the original created_at timestamp, and the changes should be persisted in the database.

**Validates: Requirements 3.4**

### Property 12: Test Runs Avoid Camunda

*For any* workflow and input data, executing a test run should return mock execution results without making any HTTP calls to the Camunda REST API.

**Validates: Requirements 3.7**

### Property 13: Deployment Creation with Rollout

*For any* authenticated tenant deploying a workflow to an environment with a specified rollout percentage (0-100), the system should create a deployment record with the correct rollout percentage, deployment key, and status.

**Validates: Requirements 4.1**

### Property 14: Non-Test Deployments Call Camunda

*For any* workflow deployment to a non-test environment (non-prod or production) with BPMN XML, the system should make a deployment call to Camunda including the tenant ID for isolation, and store the Camunda deployment ID in the metadata.

**Validates: Requirements 4.2, 5.1**

### Property 15: Test Deployments Skip Camunda

*For any* workflow deployment to the test environment, the system should create the deployment record without making any calls to the Camunda REST API.

**Validates: Requirements 4.3**

### Property 16: Deployment Promotion Validation

*For any* deployment, promoting to a higher environment (test → non-prod → production) should succeed and create a new deployment, while promoting to a lower or equal environment should fail with an error.

**Validates: Requirements 4.5, 4.6**

### Property 17: Deployment Promotion Creates New Deployment

*For any* valid deployment promotion, the system should create a new deployment in the target environment, mark the source deployment's promoted_at timestamp, and the new deployment should have the specified rollout percentage.

**Validates: Requirements 4.5**

### Property 18: Rollout Percentage Updates

*For any* deployment, updating the rollout percentage to a value between 0 and 100 should persist the new percentage in the database.

**Validates: Requirements 4.7**

### Property 19: Workflow Execution Starts Process

*For any* deployment in a non-test environment, executing the workflow with input data should start a Camunda process instance and create a workflow_execution record with the instance key and input data.

**Validates: Requirements 4.8**

### Property 20: BPMN History Time To Live

*For any* BPMN XML that lacks a historyTimeToLive attribute, the system should add the attribute with a value of 180 days before deploying to Camunda.

**Validates: Requirements 5.2**

### Property 21: Variable Type Conversion

*For any* input variables being sent to Camunda, the system should convert them to Camunda's typed variable format, mapping String → String, Integer → Integer, Double → Double, Boolean → Boolean, and complex objects → Json.

**Validates: Requirements 5.3, 5.4**

### Property 22: Camunda Error Handling

*For any* Camunda API call that fails, the system should catch the error and return a descriptive error message to the caller, not exposing internal Camunda error details.

**Validates: Requirements 5.5**

### Property 23: Foreign Key Cascade Deletes

*For any* tenant, deleting the tenant should cascade delete all related workflows, deployments, and workflow executions due to foreign key constraints.

**Validates: Requirements 6.8**

### Property 24: Consistent Error Response Format

*For any* error condition (unhandled exception, constraint violation, not found, authentication failure), the system should return an error response with consistent structure containing an "error" field with a descriptive message, and optionally a "stack" field in development mode only.

**Validates: Requirements 7.16, 9.1, 9.2, 9.3, 9.4, 9.5**

### Property 25: Required Configuration Validation

*For any* startup attempt without required configuration (DATABASE_URL or JWT_SECRET), the system should fail to start with a clear error message indicating which configuration is missing.

**Validates: Requirements 8.6, 8.7**

### Property 26: Comprehensive Request Logging

*For any* incoming HTTP request, the system should log the request method, path, and response status at INFO level, and for any error, should log the full stack trace at ERROR level.

**Validates: Requirements 9.6, 9.7, 9.8**

### Property 27: CORS Configuration by Environment

*For any* request in development mode, CORS should allow all origins, while in production mode, CORS should only allow configured origins.

**Validates: Requirements 10.1, 10.2**

### Property 28: Request Size Limits

*For any* JSON request body up to 10MB in size, the system should accept and parse it successfully, while requests exceeding 10MB should be rejected.

**Validates: Requirements 10.3**

### Property 29: Security Headers Present

*For any* HTTP response, the system should include security headers: X-Content-Type-Options, X-Frame-Options, and X-XSS-Protection.

**Validates: Requirements 10.4**

### Property 30: Database Connection Retry

*For any* startup attempt when the database is unavailable, the system should retry the connection with exponential backoff rather than failing immediately.

**Validates: Requirements 14.7**

### Property 31: JWT Interoperability

*For any* JWT token generated by the Node.js backend, the Java system should be able to validate and extract the tenant context from it. Conversely, for any JWT token generated by the Java system, it should use the same signing algorithm (HS256) and secret, making it valid for the Node.js backend.

**Validates: Requirements 15.2, 15.3**

### Property 32: Password Hash Compatibility

*For any* password hashed by the Node.js backend using BCrypt, the Java system should be able to validate it successfully. For any password hashed by the Java system, it should use BCrypt with work factor >= 10.

**Validates: Requirements 15.4**

### Property 33: JSON Serialization Compatibility

*For any* JSONB data (features, metadata, input_data, output_data) written by the Node.js backend, the Java system should be able to read and deserialize it correctly. For any JSONB data written by the Java system, it should be readable by the Node.js backend.

**Validates: Requirements 15.1, 15.5**

### Property 34: UUID Generation Compatibility

*For any* entity created by the Java system, the generated UUID should be compatible with PostgreSQL's UUID type and should be readable by the Node.js backend.

**Validates: Requirements 15.6**

### Property 35: Timestamp Format Compatibility

*For any* timestamp stored by the Java system, it should use the same format and timezone handling as the Node.js backend, ensuring timestamps are interoperable between both systems.

**Validates: Requirements 15.7**


## Error Handling

### Exception Hierarchy

```java
// Base exception
public class WorkflowSaasException extends RuntimeException {
    private final int statusCode;
    
    public WorkflowSaasException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}

// Specific exceptions
public class ResourceNotFoundException extends WorkflowSaasException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}

public class UnauthorizedException extends WorkflowSaasException {
    public UnauthorizedException(String message) {
        super(message, 401);
    }
}

public class BadRequestException extends WorkflowSaasException {
    public BadRequestException(String message) {
        super(message, 400);
    }
}

public class CamundaIntegrationException extends WorkflowSaasException {
    public CamundaIntegrationException(String message) {
        super(message, 500);
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:production}")
    private String activeProfile;
    
    @ExceptionHandler(WorkflowSaasException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowSaasException(WorkflowSaasException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            isDevelopment() ? ex.getStackTrace().toString() : null
        );
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data constraint violation";
        if (ex.getMessage().contains("unique")) {
            message = "A record with this value already exists";
        }
        ErrorResponse error = new ErrorResponse(message, isDevelopment() ? ex.getMessage() : null);
        return ResponseEntity.status(400).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        ErrorResponse error = new ErrorResponse(
            "Internal server error",
            isDevelopment() ? ex.getStackTrace().toString() : null
        );
        return ResponseEntity.status(500).body(error);
    }
    
    private boolean isDevelopment() {
        return "development".equals(activeProfile) || "dev".equals(activeProfile);
    }
}
```

### Error Response Format

All errors follow this consistent format:

```json
{
  "error": "Descriptive error message",
  "stack": "Stack trace (development only)"
}
```

### HTTP Status Code Mapping

- **200 OK**: Successful GET, PUT, PATCH requests
- **201 Created**: Successful POST requests creating resources
- **400 Bad Request**: Invalid input, constraint violations
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Valid token but insufficient permissions
- **404 Not Found**: Resource doesn't exist or doesn't belong to tenant
- **500 Internal Server Error**: Unhandled exceptions, Camunda failures


## Testing Strategy

### Dual Testing Approach

The migration requires both unit tests and property-based tests to ensure comprehensive coverage and functional equivalence with the Node.js backend.

**Unit Tests:**
- Verify specific examples and edge cases
- Test integration points between components
- Validate error conditions and boundary cases
- Test Spring Security configuration
- Test database schema and constraints
- Test Camunda integration with mock servers

**Property-Based Tests:**
- Verify universal properties across all inputs
- Test tenant isolation with randomized data
- Validate JWT token generation and validation
- Test data serialization compatibility
- Verify API contract compliance
- Test concurrent access patterns

### Property-Based Testing Configuration

**Library:** Use **jqwik** (Java property-based testing library)

**Configuration:**
- Minimum 100 iterations per property test
- Each test tagged with feature name and property number
- Tag format: `@Tag("nodejs-to-java-migration")` and `@Tag("Property-N")`

**Example Property Test Structure:**

```java
@Property(tries = 100)
@Tag("nodejs-to-java-migration")
@Tag("Property-8")
void tenantDataIsolation(@ForAll("tenants") Tenant tenantA, 
                         @ForAll("tenants") Tenant tenantB,
                         @ForAll("workflows") Workflow workflow) {
    // Property 8: Tenant Data Isolation
    // For any two different tenants A and B, tenant A should never 
    // be able to access tenant B's workflows
    
    assumeThat(tenantA.getId()).isNotEqualTo(tenantB.getId());
    
    // Create workflow for tenant B
    workflow.setTenantId(tenantB.getId());
    workflowRepository.save(workflow);
    
    // Try to access as tenant A
    String tokenA = jwtTokenProvider.generateToken(tenantA.getId(), tenantA.getEmail());
    
    ResponseEntity<?> response = restTemplate.exchange(
        "/api/workflows/" + workflow.getId(),
        HttpMethod.GET,
        new HttpEntity<>(createAuthHeaders(tokenA)),
        Workflow.class
    );
    
    // Should return 404, not the workflow
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
}
```

### Test Coverage Requirements

**Minimum Coverage Targets:**
- Service layer: 90% line coverage
- Controller layer: 85% line coverage
- Repository layer: 80% line coverage (mostly Spring Data JPA)
- Security components: 95% line coverage
- Overall business logic: 80% line coverage

### Integration Testing

**Test Containers:**
- Use Testcontainers for PostgreSQL integration tests
- Spin up real PostgreSQL instance for repository tests
- Test actual database constraints and JSONB operations

**Camunda Integration:**
- Use WireMock to mock Camunda REST API
- Test request/response formats match Camunda expectations
- Test error handling for Camunda failures

**API Contract Testing:**
- Test all endpoints match Node.js backend exactly
- Verify request/response JSON structure
- Test HTTP status codes match
- Validate error response format

### Compatibility Testing

**JWT Interoperability:**
- Generate tokens with Node.js, validate with Java
- Generate tokens with Java, validate with Node.js
- Test token expiration handling

**Password Compatibility:**
- Hash passwords with Node.js BCrypt, validate with Java
- Hash passwords with Java BCrypt, validate with Node.js
- Verify work factor compatibility

**Data Format Compatibility:**
- Write JSONB data with Node.js, read with Java
- Write JSONB data with Java, read with Node.js
- Test UUID generation compatibility
- Test timestamp format compatibility

### Performance Testing

**Startup Time:**
- Verify application starts in under 30 seconds
- Test with cold database connection
- Test with warm database connection

**Response Time:**
- API endpoints should respond within 200ms for simple queries
- Complex queries (with joins) should respond within 500ms
- Camunda deployments may take longer (acceptable up to 5 seconds)

### Test Organization

```
src/test/java/
├── unit/                          # Unit tests
│   ├── service/
│   ├── security/
│   └── util/
├── integration/                   # Integration tests
│   ├── controller/
│   ├── repository/
│   └── camunda/
├── property/                      # Property-based tests
│   ├── TenantIsolationProperties.java
│   ├── AuthenticationProperties.java
│   ├── WorkflowProperties.java
│   ├── DeploymentProperties.java
│   └── CompatibilityProperties.java
└── compatibility/                 # Cross-system compatibility tests
    ├── JwtCompatibilityTest.java
    ├── PasswordCompatibilityTest.java
    └── DataFormatCompatibilityTest.java
```

### Continuous Integration

**Pre-commit:**
- Run unit tests
- Run static analysis (SpotBugs, Checkstyle)
- Verify code formatting

**CI Pipeline:**
- Run all unit tests
- Run integration tests with Testcontainers
- Run property-based tests (100 iterations)
- Generate coverage report
- Fail if coverage below thresholds

**Pre-deployment:**
- Run full test suite
- Run compatibility tests against Node.js backend
- Run performance tests
- Verify all properties pass

