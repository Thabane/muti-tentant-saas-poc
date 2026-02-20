# Project Structure

## Repository Layout

```
.
├── .kiro/                      # Kiro configuration
│   ├── specs/                  # Feature specifications
│   ├── steering/               # Steering rules (this file)
│   └── hooks/                  # Agent hooks
├── java-backend/               # Spring Boot backend
├── frontend/                   # React frontend
├── backend/                    # Legacy Node.js backend (deprecated)
├── docker-compose.yml          # Local infrastructure
└── *.md                        # Documentation
```

## Java Backend Structure

```
java-backend/
├── src/main/java/com/workflowsaas/
│   ├── Application.java        # Spring Boot entry point
│   ├── config/                 # Spring configuration classes
│   │   ├── CamundaConfig.java
│   │   ├── CorsConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/             # REST API endpoints
│   │   ├── TenantController.java
│   │   ├── WorkflowController.java
│   │   ├── DeploymentController.java
│   │   └── AppController.java
│   ├── dto/                    # Data transfer objects
│   │   ├── request/            # Request DTOs
│   │   ├── response/           # Response DTOs
│   │   └── camunda/            # Camunda-specific DTOs
│   ├── entity/                 # JPA entities (database models)
│   │   ├── Tenant.java
│   │   ├── Workflow.java
│   │   ├── Deployment.java
│   │   ├── App.java
│   │   └── WorkflowExecution.java
│   ├── repository/             # Spring Data JPA repositories
│   ├── service/                # Business logic layer
│   ├── security/               # Security components (JWT, filters)
│   └── exception/              # Custom exceptions and handlers
├── src/main/resources/
│   ├── application.properties  # Spring Boot configuration
│   └── db/changelog/           # Liquibase migrations
├── src/test/java/              # Test files (mirrors main structure)
├── pom.xml                     # Maven dependencies
├── .env                        # Environment variables (not in git)
└── README.md
```

## Frontend Structure

```
frontend/
├── src/
│   ├── main.jsx                # React entry point
│   ├── App.jsx                 # Root component with routing
│   ├── index.css               # Global styles
│   ├── pages/                  # Page components
│   │   ├── Login.jsx
│   │   ├── Register.jsx
│   │   ├── Dashboard.jsx
│   │   ├── Onboarding.jsx
│   │   ├── Apps.jsx
│   │   ├── WorkflowDesigner.jsx
│   │   └── Deployments.jsx
│   └── services/
│       └── api.js              # Axios HTTP client
├── index.html                  # HTML template
├── package.json
└── vite.config.js              # Vite configuration
```

## Architecture Patterns

### Backend Layering

1. **Controller Layer**: REST endpoints, request validation, HTTP concerns
2. **Service Layer**: Business logic, tenant isolation, orchestration
3. **Repository Layer**: Data access via Spring Data JPA
4. **Entity Layer**: JPA entities with database mappings

### Naming Conventions

- **Entities**: Singular nouns (Tenant, Workflow, App)
- **Controllers**: `{Entity}Controller` (TenantController)
- **Services**: `{Entity}Service` (TenantService)
- **Repositories**: `{Entity}Repository` (TenantRepository)
- **DTOs**: `{Action}{Entity}Request/Response` (CreateAppRequest, AppResponse)
- **Packages**: lowercase, no underscores (com.workflowsaas.controller)

### Code Style

- Use Lombok annotations (@Data, @Builder, etc.) to reduce boilerplate
- Constructor injection for dependencies (no @Autowired on fields)
- Package-private classes when possible
- Javadoc on public APIs and complex logic
- RESTful URL patterns: `/api/{resource}` and `/api/{resource}/{id}`

### Multi-Tenancy

- Tenant ID extracted from JWT token in security filter
- All queries automatically scoped to current tenant
- Services enforce tenant isolation
- Database uses tenant_id foreign keys with indexes

### Testing Structure

- Unit tests: Test single class in isolation with mocks
- Integration tests: Use @SpringBootTest with H2 database
- Property-based tests: Use jqwik for universal properties
- Test files mirror source structure in src/test/java/

## Database Schema

Key tables:
- `tenants`: Tenant accounts with authentication
- `apps`: Application containers for workflows
- `workflows`: BPMN workflow definitions
- `deployments`: Environment-specific deployments
- `workflow_executions`: Execution history and results

All tables include tenant_id for isolation (except tenants table).

## Frontend Routing

- `/login`, `/register`: Public authentication pages
- `/onboarding`: First-time setup wizard
- `/dashboard`: Main landing page
- `/apps`: App management
- `/workflows/:id?`: BPMN designer (create/edit)
- `/deployments`: Deployment management

Protected routes require JWT token in localStorage.
