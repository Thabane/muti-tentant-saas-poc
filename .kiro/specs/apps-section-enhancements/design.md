# Design Document: Apps Section Enhancements

## Overview

This design transforms the existing "Workflows" section into an "Apps" section with a hierarchical structure that groups BPMN services and their associated DMN decision logic. The system will automatically generate RESTful API paths for each service and assign unique API keys to apps for authentication.

The implementation involves:
- Creating a new App entity to serve as a container for related resources
- Establishing relationships between App → BPMN → DMN
- Implementing automatic API path generation based on service names
- Adding API key generation and management for apps
- Updating the React frontend to display the hierarchical structure
- Migrating existing workflow data to the new structure

## Architecture

### System Components

The system follows a layered architecture:

```
┌─────────────────────────────────────────┐
│         React Frontend (UI)             │
│  - Apps Section Component               │
│  - Tree View for BPMN/DMN Hierarchy     │
│  - API Key Management UI                │
└─────────────────────────────────────────┘
                  │
                  │ REST API
                  ▼
┌─────────────────────────────────────────┐
│      Spring Boot Backend                │
│  - AppController                        │
│  - AppService                           │
│  - API Path Generator                   │
│  - API Key Generator & Validator        │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         PostgreSQL Database             │
│  - apps table                           │
│  - workflows table (updated)            │
│  - api_keys table                       │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│         Camunda Engine                  │
│  - Process Orchestration                │
└─────────────────────────────────────────┘
```

### Key Design Decisions

1. **App as First-Class Entity**: Apps are independent entities that own BPMN and DMN resources, enabling logical grouping and API key scoping
2. **Hierarchical Relationships**: App → BPMN (one-to-many), BPMN → DMN (one-to-many)
3. **API Path Pattern**: Follows RESTful conventions with versioning: `/{tenant-id}/{sub-service}/{service-name}/v1`
4. **API Key Storage**: Hashed storage with secure generation using cryptographically secure random generators
5. **Backward Compatibility**: Migration strategy ensures existing workflows continue to function

## Components and Interfaces

### Backend Components

#### 1. App Entity

```java
@Entity
@Table(name = "apps")
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private UUID tenantId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String apiKeyHash;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL)
    private List<Workflow> workflows;
}
```

#### 2. Updated Workflow Entity

```java
@Entity
@Table(name = "workflows")
public class Workflow {
    // Existing fields...
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id")
    private App app;
    
    @Column(name = "parent_workflow_id")
    private UUID parentWorkflowId;  // For DMN → BPMN relationship
    
    @Column(name = "api_path")
    private String apiPath;  // Generated path for BPMN services
    
    @Column(name = "sub_service")
    private String subService;  // Sub-service grouping
}
```

#### 3. AppService

```java
public interface AppService {
    App createApp(CreateAppRequest request);
    List<App> getAllApps();
    App getAppById(UUID id);
    App updateApp(UUID id, UpdateAppRequest request);
    void deleteApp(UUID id);
    String regenerateApiKey(UUID appId);
    boolean validateApiKey(String apiKey);
}
```

#### 4. ApiPathGenerator

```java
public interface ApiPathGenerator {
    /**
     * Generates API path following pattern: /{tenant-id}/{sub-service}/{service-name}/v1
     * - Converts service name to lowercase
     * - Replaces spaces with hyphens
     * - Ensures uniqueness within tenant/sub-service scope
     */
    String generateApiPath(UUID tenantId, String subService, String serviceName);
    
    /**
     * Validates that the generated path is unique
     */
    boolean isPathUnique(String apiPath);
}
```

#### 5. ApiKeyService

```java
public interface ApiKeyService {
    /**
     * Generates a cryptographically secure API key
     * Format: app_[32 random alphanumeric characters]
     */
    String generateApiKey();
    
    /**
     * Hashes API key for secure storage using BCrypt
     */
    String hashApiKey(String apiKey);
    
    /**
     * Validates API key against stored hash
     */
    boolean validateApiKey(String apiKey, String hash);
}
```

#### 6. AppController

```java
@RestController
@RequestMapping("/api/apps")
public class AppController {
    @PostMapping
    ResponseEntity<AppResponse> createApp(@RequestBody CreateAppRequest request);
    
    @GetMapping
    ResponseEntity<List<AppResponse>> getAllApps();
    
    @GetMapping("/{id}")
    ResponseEntity<AppResponse> getApp(@PathVariable UUID id);
    
    @PutMapping("/{id}")
    ResponseEntity<AppResponse> updateApp(@PathVariable UUID id, @RequestBody UpdateAppRequest request);
    
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteApp(@PathVariable UUID id);
    
    @PostMapping("/{id}/regenerate-key")
    ResponseEntity<ApiKeyResponse> regenerateApiKey(@PathVariable UUID id);
}
```

### Frontend Components

#### 1. AppsSection Component

```typescript
interface AppsSection {
  // Main container for the Apps section
  // Replaces the existing WorkflowsSection component
  
  props: {
    tenantId: string;
  }
  
  state: {
    apps: App[];
    selectedApp: App | null;
    loading: boolean;
  }
  
  methods: {
    loadApps(): Promise<void>;
    createApp(name: string): Promise<void>;
    deleteApp(appId: string): Promise<void>;
  }
}
```

#### 2. AppTreeView Component

```typescript
interface AppTreeView {
  // Hierarchical tree view showing App → BPMN → DMN structure
  
  props: {
    app: App;
    onSelectNode: (node: TreeNode) => void;
  }
  
  state: {
    expandedNodes: Set<string>;
  }
  
  methods: {
    toggleNode(nodeId: string): void;
    renderBpmnNode(bpmn: Workflow): ReactNode;
    renderDmnNode(dmn: Workflow): ReactNode;
  }
}
```

#### 3. ApiKeyDisplay Component

```typescript
interface ApiKeyDisplay {
  // Displays and manages API key for an app
  
  props: {
    appId: string;
    apiKey: string;
  }
  
  methods: {
    copyToClipboard(): void;
    regenerateKey(): Promise<void>;
    toggleVisibility(): void;
  }
}
```

## Data Models

### Database Schema

#### apps table

```sql
CREATE TABLE apps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    api_key_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT unique_app_name_per_tenant UNIQUE (tenant_id, name)
);

CREATE INDEX idx_apps_tenant_id ON apps(tenant_id);
CREATE INDEX idx_apps_api_key_hash ON apps(api_key_hash);
```

#### workflows table (updated)

```sql
ALTER TABLE workflows 
ADD COLUMN app_id UUID REFERENCES apps(id) ON DELETE CASCADE,
ADD COLUMN parent_workflow_id UUID REFERENCES workflows(id) ON DELETE SET NULL,
ADD COLUMN api_path VARCHAR(500),
ADD COLUMN sub_service VARCHAR(255);

CREATE INDEX idx_workflows_app_id ON workflows(app_id);
CREATE INDEX idx_workflows_parent_id ON workflows(parent_workflow_id);
CREATE INDEX idx_workflows_api_path ON workflows(api_path);
```

### API Request/Response Models

#### CreateAppRequest

```java
public record CreateAppRequest(
    @NotBlank String name
) {}
```

#### AppResponse

```java
public record AppResponse(
    UUID id,
    String name,
    String apiKey,  // Only returned on creation or regeneration
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<WorkflowSummary> workflows
) {}
```

#### WorkflowSummary

```java
public record WorkflowSummary(
    UUID id,
    String name,
    String type,  // "BPMN" or "DMN"
    String apiPath,  // Only for BPMN
    List<WorkflowSummary> children  // DMNs for BPMN parent
) {}
```

### API Path Generation Algorithm

```
Input: tenantId, subService, serviceName
Output: apiPath

1. Convert serviceName to lowercase
2. Replace all spaces with hyphens
3. Remove any special characters except hyphens
4. Construct path: "/{tenantId}/{subService}/{normalized-service-name}/v1"
5. Verify uniqueness within tenant/sub-service scope
6. If not unique, append counter: "/v1-2", "/v1-3", etc.
7. Return generated path
```

Example:
- Input: tenantId="abc-123", subService="financial", serviceName="Income Service"
- Output: "/abc-123/financial/income-service/v1"

## Correctness Properties


*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Resource Association with Apps

*For any* BPMN or DMN resource created in the system, the resource must be associated with a valid app (app_id must be non-null and reference an existing app).

**Validates: Requirements 2.2**

### Property 2: Hierarchical Resource Organization

*For any* query requesting resources, the response must organize BPMN and DMN resources under their respective app containers, with DMNs appearing as children of their parent BPMN.

**Validates: Requirements 2.1, 2.3, 3.1, 3.2, 3.4**

### Property 3: Parent-Child Referential Integrity

*For any* DMN resource, if it has a parent_workflow_id, that ID must reference a valid BPMN resource (type="BPMN") within the same app.

**Validates: Requirements 3.3, 7.4**

### Property 4: API Path Generation Pattern

*For any* BPMN service created with tenant-id, sub-service, and service-name, the generated API path must follow the pattern `/{tenant-id}/{sub-service}/{normalized-service-name}/v1` where normalized-service-name is lowercase with spaces replaced by hyphens.

**Validates: Requirements 5.1, 5.2**

### Property 5: API Path Uniqueness

*For any* two BPMN services within the same tenant and sub-service scope, their generated API paths must be unique.

**Validates: Requirements 5.4**

### Property 6: API Key Generation and Uniqueness

*For any* app created in the system, the app must have a unique API key that is different from all other apps' API keys.

**Validates: Requirements 6.1, 6.6**

### Property 7: API Key Secure Storage

*For any* app with an API key, the stored api_key_hash in the database must be a hashed value (not plaintext) and must validate successfully against the original key.

**Validates: Requirements 6.2**

### Property 8: API Key Validation

*For any* API request with an API key, validation must succeed if and only if the key matches a valid app's stored hash.

**Validates: Requirements 6.4**

### Property 9: API Key Regeneration

*For any* app, after regenerating its API key, the old key must fail validation and the new key must succeed validation.

**Validates: Requirements 6.5**

### Property 10: One-to-Many Relationships

*For any* app, it must be able to have zero or more BPMN resources associated with it, and for any BPMN, it must be able to have zero or more DMN resources associated with it.

**Validates: Requirements 7.2, 7.3**

### Property 11: Cascade Delete Behavior

*For any* app that is deleted, either all associated BPMN and DMN resources must be deleted (cascade), or the deletion must be prevented if resources exist.

**Validates: Requirements 7.5**

### Property 12: Migration Data Preservation

*For any* BPMN-DMN relationship that exists before migration, the same relationship (via parent_workflow_id) must exist after migration completes.

**Validates: Requirements 8.3**

### Property 13: Migration Completeness

*For any* BPMN resource after migration, it must have a non-null app_id, a generated API key (via its app), and a generated api_path.

**Validates: Requirements 8.2, 8.4, 8.5**

## Error Handling

### API Path Generation Errors

**Duplicate Path Detection**:
- When a generated path conflicts with an existing path, append a counter suffix
- Pattern: `/v1-2`, `/v1-3`, etc.
- Return error if unable to generate unique path after 100 attempts

**Invalid Service Name**:
- If service name contains only special characters after normalization, return error
- Minimum normalized name length: 1 character
- Error message: "Service name must contain at least one alphanumeric character"

### API Key Errors

**Invalid API Key Format**:
- API keys must match pattern: `app_[32 alphanumeric characters]`
- Return 401 Unauthorized for invalid format
- Error message: "Invalid API key format"

**API Key Not Found**:
- Return 401 Unauthorized if API key doesn't match any app
- Error message: "Invalid API key"

**API Key Regeneration Failure**:
- If regeneration fails after 10 attempts to generate unique key, return 500
- Error message: "Failed to generate unique API key"

### Relationship Errors

**Orphaned Resource Prevention**:
- Prevent creation of BPMN/DMN without valid app_id
- Return 400 Bad Request
- Error message: "Resource must be associated with an app"

**Invalid Parent Reference**:
- Prevent DMN from referencing non-existent or non-BPMN parent
- Return 400 Bad Request
- Error message: "Parent workflow must be a valid BPMN resource"

**Cross-App Reference Prevention**:
- Prevent DMN from referencing BPMN in different app
- Return 400 Bad Request
- Error message: "DMN must reference BPMN within the same app"

### Deletion Errors

**App with Resources**:
- If cascade delete is disabled, prevent deletion of app with resources
- Return 409 Conflict
- Error message: "Cannot delete app with existing resources"

**Referenced BPMN Deletion**:
- Prevent deletion of BPMN that has DMN children
- Return 409 Conflict
- Error message: "Cannot delete BPMN with associated DMN resources"

## Testing Strategy

### Dual Testing Approach

This feature requires both unit tests and property-based tests to ensure comprehensive coverage:

**Unit Tests** focus on:
- Specific examples (e.g., "Income Service" → "/tenant-id/sub-service/income-service/v1")
- Edge cases (e.g., BPMN with no DMNs, service names with special characters)
- Error conditions (e.g., invalid API keys, orphaned resources)
- Integration points between components

**Property-Based Tests** focus on:
- Universal properties that hold for all inputs
- Comprehensive input coverage through randomization
- Relationship integrity across random data sets
- API path generation across various service names

### Property-Based Testing Configuration

**Framework**: Use JUnit 5 with jqwik for Java property-based testing

**Configuration**:
- Minimum 100 iterations per property test
- Each test must reference its design document property
- Tag format: `@Tag("Feature: apps-section-enhancements, Property {number}: {property_text}")`

**Example Property Test Structure**:

```java
@Property
@Tag("Feature: apps-section-enhancements, Property 4: API Path Generation Pattern")
void apiPathFollowsPattern(@ForAll UUID tenantId, 
                          @ForAll String subService, 
                          @ForAll String serviceName) {
    String apiPath = apiPathGenerator.generateApiPath(tenantId, subService, serviceName);
    
    String normalized = serviceName.toLowerCase().replaceAll("\\s+", "-");
    String expected = String.format("/%s/%s/%s/v1", tenantId, subService, normalized);
    
    assertThat(apiPath).isEqualTo(expected);
}
```

### Test Coverage Requirements

**Backend Tests**:
- App entity CRUD operations
- API path generation algorithm
- API key generation and validation
- Relationship integrity (App → BPMN → DMN)
- Migration script execution
- Error handling for all error scenarios

**Frontend Tests**:
- Apps section rendering with "Apps" terminology
- Tree view hierarchy display
- Node expansion/collapse behavior
- API key display and copy functionality
- API key regeneration flow

**Integration Tests**:
- End-to-end app creation with BPMN and DMN
- API path uniqueness across concurrent creations
- API key authentication flow
- Migration from old to new structure

### Migration Testing

**Pre-Migration Validation**:
- Snapshot existing workflow data
- Count BPMN and DMN resources
- Document existing relationships

**Post-Migration Validation**:
- Verify all BPMNs have app associations
- Verify all DMNs maintain parent relationships
- Verify all apps have API keys
- Verify all BPMNs have API paths
- Compare counts with pre-migration snapshot

**Rollback Testing**:
- Test migration rollback script
- Verify data restoration to pre-migration state
