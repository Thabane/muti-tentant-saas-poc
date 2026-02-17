# Setup Instructions

## Prerequisites

- Node.js 18+ and npm
- Docker and Docker Compose
- PostgreSQL (via Docker)
- Java 17+ and Maven 3.9+ (for Java backend)

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL and Camunda BPM Platform 7.

### 2. Install Dependencies

```bash
npm run install:all
```

### 3. Configure Backend

```bash
cd backend
cp .env.example .env
```

Edit `.env` with your configuration (defaults work for local development).

### 4. Initialize Database

Start the backend once to create tables:

```bash
npm run dev:backend
```

The database schema will be created automatically on first run.

### 5. Start Development Servers

In separate terminals:

```bash
# Terminal 1: Backend
npm run dev:backend

# Terminal 2: Frontend
npm run dev:frontend
```

Or run both together:

```bash
npm run dev
```

### 6. Access the Application

- Frontend: http://localhost:5173
- Backend API (Node.js): http://localhost:3000
- Backend API (Java): http://localhost:3000 (when running Java backend)
- Camunda Cockpit: http://localhost:8080/camunda (admin/admin)
- Camunda REST API: http://localhost:8080/engine-rest

## Java Backend Setup (Migration)

The project includes a Java 21 Spring Boot backend as a migration path from Node.js. Both backends can coexist and share the same database.

### Prerequisites for Java Backend

- Java 17 or higher (Java 21 recommended)
- Maven 3.9+
- Same PostgreSQL database as Node.js backend

### Build and Test Java Backend

```bash
cd java-backend

# Run tests
mvn test

# Build the application
mvn clean package

# Run the application
java -jar target/workflow-saas-1.0.0.jar
```

### Configure Java Backend

Create `java-backend/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=3000

# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:postgresql://localhost:5432/workflow_saas}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# JWT Configuration
jwt.secret=${JWT_SECRET:your-secret-key-min-256-bits}
jwt.expiration=604800000

# Camunda Configuration
camunda.rest.url=${CAMUNDA_REST_URL:http://localhost:8080/engine-rest}

# Logging
logging.level.com.workflowsaas=INFO
```

### Run Java Backend

```bash
cd java-backend
mvn spring-boot:run
```

Or using the packaged JAR:

```bash
java -jar target/workflow-saas-1.0.0.jar
```

### Migration Notes

- The Java backend uses the same database schema as Node.js
- JWT tokens are interoperable between both backends
- Password hashes (BCrypt) are compatible
- Both backends can run simultaneously for gradual migration
- API endpoints are identical for frontend compatibility

## Usage Flow

### 1. Register a Tenant

- Navigate to http://localhost:5173/register
- Create an account with organization details
- Complete the onboarding flow

### 2. Create a Workflow

- Go to Dashboard → Create Workflow
- Use the visual BPMN designer (bpmn.io)
- Design your process flow
- Save the workflow

### 3. Test the Workflow

- In the workflow designer, use the Test panel
- Provide JSON input data
- Click "Test Run" to simulate execution
- Review the output

### 4. Deploy to Non-Production

- Navigate to Deployments
- Click "New Deployment"
- Select your workflow
- Choose "Non-Production" environment
- Set rollout percentage (e.g., 50% for gradual rollout)
- Deploy

### 5. Promote to Production

- Find your non-prod deployment
- Click "Promote"
- Specify rollout percentage
- Confirm promotion

### 6. Manage Rollouts

- Use "Update Rollout" to gradually increase deployment percentage
- Monitor active deployments per environment
- Track deployment history

## Architecture Highlights

### Tenant Isolation

- Each tenant has a unique ID in the database
- All queries are scoped by tenant_id
- JWT tokens include tenant context
- Middleware enforces tenant isolation

### Environment Strategy

- **Test**: Simulation only, no Camunda engine execution
- **Non-Prod**: Real deployment to Camunda with tenant isolation
- **Production**: Live environment with selective rollouts and Camunda execution

### Selective Rollouts

- Deployments include rollout_percentage (0-100)
- Allows gradual feature releases
- Can be updated dynamically
- Supports A/B testing and canary deployments

## API Endpoints

### Tenants
- POST /api/tenants/register - Register new tenant
- POST /api/tenants/login - Login
- GET /api/tenants/me - Get current tenant
- PATCH /api/tenants/onboarding - Complete onboarding

### Workflows
- POST /api/workflows - Create workflow
- GET /api/workflows - List workflows
- GET /api/workflows/:id - Get workflow
- PUT /api/workflows/:id - Update workflow
- POST /api/workflows/:id/test - Test run workflow

### Deployments
- POST /api/deployments - Create deployment
- GET /api/deployments - List deployments
- POST /api/deployments/:id/promote - Promote to higher environment
- POST /api/deployments/:id/rollout - Update rollout percentage
- POST /api/deployments/:id/execute - Execute workflow in non-prod/production

## Database Schema

### tenants
- Stores tenant information
- Features configuration (JSONB)
- Onboarding status

### workflows
- BPMN/DMN definitions
- Version tracking
- Tenant-scoped

### deployments
- Environment tracking (test/non-prod/production)
- Rollout percentage
- Deployment metadata

### workflow_executions
- Test run history
- Input/output data
- Execution status

## Next Steps

### Production Readiness

1. Add proper authentication (OAuth2, SAML)
2. Implement rate limiting
3. Add monitoring and logging (Prometheus, Grafana)
4. Set up CI/CD pipelines
5. Configure SSL/TLS
6. Implement backup strategies
7. Add comprehensive error handling
8. Set up alerting

### Feature Enhancements

1. DMN decision table editor
2. Workflow versioning UI
3. Execution history and analytics
4. Role-based access control (RBAC)
5. Webhook integrations
6. Custom task handlers
7. Workflow templates library
8. Collaborative editing

### Scalability

1. Implement caching (Redis)
2. Add message queue (RabbitMQ/Kafka)
3. Horizontal scaling with load balancer
4. Database read replicas
5. CDN for static assets
6. Microservices architecture
