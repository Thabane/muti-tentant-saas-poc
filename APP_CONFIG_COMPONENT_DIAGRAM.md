# App Configuration Management - Component Diagram

## System Architecture Overview

```mermaid
graph TB
    subgraph "Frontend Layer - React"
        A[Apps Page] --> B[AppDetail Page]
        B --> C[Configuration Tab]
        C --> D[SourceSelector]
        C --> E[EnrichmentApiSection]
        C --> F[FileUpload Components]
        C --> G[PublisherSection]
        E --> H[EnrichmentApiEntry]
        H --> I[FileUpload]
        H --> J[KeyValueEditor]
        G --> K[ApiPublisherConfig]
        G --> L[EehPublisherConfig]
        L --> M[WarehousePublisherConfig]
        B --> N[API Service]
    end

    subgraph "Backend Layer - Spring Boot"
        O[AppConfigController] --> P[AppConfigService]
        P --> Q[FileValidationService]
        P --> R[AppConfigRepository]
        P --> S[EnrichmentApiRepository]
        P --> T[PublisherConfigRepository]
        P --> U[WarehousePublisherRepository]
    end

    subgraph "Data Layer - PostgreSQL"
        V[(app_configurations)]
        W[(enrichment_api_configs)]
        X[(publisher_configs)]
        Y[(warehouse_publisher_configs)]
        Z[(apps)]
        AA[(tenants)]
    end

    N -->|HTTP/REST| O
    R --> V
    S --> W
    T --> X
    U --> Y
    V -->|FK| Z
    V -->|FK| AA
    W -->|FK| V
    X -->|FK| V
    Y -->|FK| X
```

## Detailed Component Breakdown

### Frontend Components

```mermaid
graph LR
    subgraph "Page Components"
        A1[Apps.jsx]
        A2[AppDetail.jsx]
    end

    subgraph "Configuration Components"
        B1[SourceSelector.jsx]
        B2[EnrichmentApiSection.jsx]
        B3[EnrichmentApiEntry.jsx]
        B4[PublisherSection.jsx]
        B5[ApiPublisherConfig.jsx]
        B6[EehPublisherConfig.jsx]
        B7[WarehousePublisherConfig.jsx]
    end

    subgraph "Shared Components"
        C1[FileUpload.jsx]
        C2[KeyValueEditor.jsx]
        C3[ApiKeyDisplay.jsx]
        C4[AppTreeView.jsx]
    end

    subgraph "Services"
        D1[api.js - appAPI]
    end

    A1 --> A2
    A2 --> B1
    A2 --> B2
    A2 --> B4
    B2 --> B3
    B3 --> C1
    B3 --> C2
    B4 --> B5
    B4 --> B6
    B6 --> B7
    B5 --> C1
    B5 --> C2
    B7 --> C1
    A2 --> C3
    A1 --> C4
    A2 --> D1
```

### Backend Components

```mermaid
graph TB
    subgraph "Controller Layer"
        E1[AppConfigController]
        E2[AppController]
        E3[TenantController]
    end

    subgraph "Service Layer"
        F1[AppConfigServiceImpl]
        F2[FileValidationServiceImpl]
        F3[AppServiceImpl]
        F4[TenantService]
    end

    subgraph "Repository Layer"
        G1[AppConfigRepository]
        G2[EnrichmentApiConfigRepository]
        G3[PublisherConfigRepository]
        G4[WarehousePublisherConfigRepository]
        G5[AppRepository]
        G6[TenantRepository]
    end

    subgraph "DTOs"
        H1[Request DTOs]
        H2[Response DTOs]
    end

    subgraph "Entities"
        I1[AppConfiguration]
        I2[EnrichmentApiConfig]
        I3[PublisherConfig]
        I4[WarehousePublisherConfig]
        I5[App]
        I6[Tenant]
    end

    E1 --> F1
    E1 --> H1
    E1 --> H2
    F1 --> F2
    F1 --> G1
    F1 --> G2
    F1 --> G3
    F1 --> G4
    F1 --> G5
    G1 --> I1
    G2 --> I2
    G3 --> I3
    G4 --> I4
    G5 --> I5
    G6 --> I6
```

## Data Flow Diagrams

### Configuration Save Flow

```mermaid
sequenceDiagram
    participant User
    participant AppDetail
    participant API
    participant Controller
    participant Service
    participant Validation
    participant Repository
    participant Database

    User->>AppDetail: Click Save Configuration
    AppDetail->>AppDetail: Validate source selected
    alt Source not selected
        AppDetail->>User: Show error message
    else Source selected
        AppDetail->>API: POST/PUT /api/v1/apps/{id}/config
        API->>Controller: HTTP Request
        Controller->>Service: createConfig/updateConfig
        Service->>Validation: Validate files
        alt Validation fails
            Validation-->>Service: Throw FileValidationException
            Service-->>Controller: Exception
            Controller-->>API: 400 Bad Request
            API-->>AppDetail: Error response
            AppDetail->>User: Display error
        else Validation passes
            Service->>Repository: Save configuration
            Repository->>Database: INSERT/UPDATE
            Database-->>Repository: Success
            Repository-->>Service: Saved entity
            Service-->>Controller: Response DTO
            Controller-->>API: 200 OK
            API-->>AppDetail: Success response
            AppDetail->>User: Show success message
        end
    end
```

### Configuration Load Flow

```mermaid
sequenceDiagram
    participant User
    participant AppDetail
    participant API
    participant Controller
    participant Service
    participant Repository
    participant Database

    User->>AppDetail: Navigate to Configuration tab
    AppDetail->>API: GET /api/v1/apps/{id}/config
    API->>Controller: HTTP Request
    Controller->>Service: getConfig(appId)
    Service->>Repository: findByAppIdAndTenantId
    Repository->>Database: SELECT query
    alt Config exists
        Database-->>Repository: Configuration data
        Repository-->>Service: Entity
        Service-->>Controller: Response DTO
        Controller-->>API: 200 OK
        API-->>AppDetail: Configuration data
        AppDetail->>User: Display form with data
    else Config not found
        Database-->>Repository: Empty result
        Repository-->>Service: Empty Optional
        Service-->>Controller: Throw NotFoundException
        Controller-->>API: 404 Not Found
        API-->>AppDetail: 404 response
        AppDetail->>User: Initialize empty form
    end
```

## Component Responsibilities

### Frontend Components

| Component | Responsibility |
|-----------|---------------|
| **Apps.jsx** | List all apps, create new apps, navigate to app details |
| **AppDetail.jsx** | Main page for app management, tab navigation, state management |
| **SourceSelector.jsx** | Dropdown for selecting data source (EEH/API) |
| **EnrichmentApiSection.jsx** | Container for enrichment API entries, add/remove logic |
| **EnrichmentApiEntry.jsx** | Single enrichment API configuration form |
| **PublisherSection.jsx** | Publisher type selection and configuration |
| **ApiPublisherConfig.jsx** | API publisher configuration form |
| **EehPublisherConfig.jsx** | EEH publisher wrapper |
| **WarehousePublisherConfig.jsx** | Warehouse publisher configuration form |
| **FileUpload.jsx** | Reusable file upload with validation display |
| **KeyValueEditor.jsx** | Dynamic key-value pair editor |
| **ApiKeyDisplay.jsx** | Display, copy, and regenerate API keys |
| **AppTreeView.jsx** | Hierarchical tree view of apps and workflows |

### Backend Components

| Component | Responsibility |
|-----------|---------------|
| **AppConfigController** | REST endpoints for configuration CRUD operations |
| **AppConfigServiceImpl** | Business logic, validation orchestration, tenant isolation |
| **FileValidationServiceImpl** | Validate OpenAPI, Avro, CSV, request mappings |
| **AppConfigRepository** | Data access for app configurations |
| **EnrichmentApiConfigRepository** | Data access for enrichment API configs |
| **PublisherConfigRepository** | Data access for publisher configs |
| **WarehousePublisherConfigRepository** | Data access for warehouse publisher configs |

### Database Tables

| Table | Purpose |
|-------|---------|
| **app_configurations** | Main configuration data (source, files) |
| **enrichment_api_configs** | Enrichment API configurations (OpenAPI, mappings) |
| **publisher_configs** | Publisher configurations (type, OpenAPI, mappings) |
| **warehouse_publisher_configs** | Warehouse-specific publisher data (Avro schema) |
| **apps** | Application containers |
| **tenants** | Multi-tenant isolation |

## API Endpoints

```
GET    /api/v1/apps/{appId}/config
POST   /api/v1/apps/{appId}/config
PUT    /api/v1/apps/{appId}/config
DELETE /api/v1/apps/{appId}/config/enrichment-apis/{id}
DELETE /api/v1/apps/{appId}/config/publisher
```

## Validation Flow

```mermaid
graph TD
    A[User Input] --> B{Source Selected?}
    B -->|No| C[Show Error: Select source]
    B -->|Yes| D{Files Uploaded?}
    D -->|Yes| E[Validate File Content]
    D -->|No| F[Skip File Validation]
    E --> G{Valid?}
    G -->|No| H[Show File Error]
    G -->|Yes| I[Validate Request Mappings]
    F --> I
    I --> J{Valid Format?}
    J -->|No| K[Show Mapping Error]
    J -->|Yes| L[Save Configuration]
    L --> M[Show Success]
    C --> N[Prevent Save]
    H --> N
    K --> N
```

## Error Handling Flow

```mermaid
graph TD
    A[Backend Error] --> B{Error Type?}
    B -->|Validation Error| C[Extract errors object]
    B -->|General Error| D[Extract error message]
    C --> E{File Error?}
    E -->|CSV| F[Map to parametersFile/modelFile]
    E -->|OpenAPI| G[Map to enrichmentApis]
    E -->|Avro| H[Map to publisher]
    E -->|Generic| I[Set as general error]
    D --> I
    F --> J[Display in FileUpload component]
    G --> K[Display in EnrichmentApiSection]
    H --> L[Display in PublisherSection]
    I --> M[Display at top of form]
```

## Technology Stack

### Frontend
- React 18.2.0
- React Router 6.20.1
- Axios for HTTP
- Vite 5.0.8 (build tool)
- Vitest (testing)
- @testing-library/react (component testing)

### Backend
- Java 17
- Spring Boot 3.1.5
- Spring Data JPA
- PostgreSQL 15
- Liquibase (migrations)
- Lombok (boilerplate reduction)
- jqwik (property-based testing)

### Infrastructure
- Docker Compose
- PostgreSQL 15 (Alpine)

## Multi-Tenancy

All operations are tenant-scoped:

```mermaid
graph LR
    A[JWT Token] --> B[Extract Tenant ID]
    B --> C[TenantContextHolder]
    C --> D[Service Layer]
    D --> E{Validate Tenant}
    E -->|Match| F[Allow Operation]
    E -->|Mismatch| G[Throw UnauthorizedException]
    F --> H[Repository with tenant_id filter]
    H --> I[Database Query]
```

## Key Design Patterns

1. **Repository Pattern** - Data access abstraction
2. **Service Layer Pattern** - Business logic separation
3. **DTO Pattern** - Request/Response separation from entities
4. **Component Composition** - Reusable React components
5. **Controlled Components** - React form state management
6. **Constructor Injection** - Spring dependency injection
7. **Multi-Tenancy** - Tenant isolation at all layers
