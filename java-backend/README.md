# Workflow SaaS Platform - Java Backend

This is the Java 17 Spring Boot implementation of the multi-tenant workflow SaaS platform, migrated from Node.js/Express.

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- PostgreSQL 14+
- Docker and Docker Compose (for infrastructure)

## Quick Start

### 1. Start Infrastructure

From the project root:

```bash
docker-compose up -d
```

This starts PostgreSQL and Camunda BPM Platform 7.

### 2. Configure Environment

```bash
cd java-backend
cp .env.example .env
```

Edit `.env` with your configuration. For local development with the Node.js backend running simultaneously, use a different port (e.g., 3001).

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or build and run the JAR:

```bash
mvn clean package
java -jar target/workflow-saas-1.0.0.jar
```

### 4. Access the Application

- Health endpoint: http://localhost:3000/health
- API endpoints: http://localhost:3000/api/*

## API Endpoints

All endpoints are identical to the Node.js backend for backward compatibility.

### Tenants
- `POST /api/tenants/register` - Register new tenant
- `POST /api/tenants/login` - Login
- `GET /api/tenants/me` - Get current tenant (authenticated)
- `PATCH /api/tenants/onboarding` - Complete onboarding (authenticated)

### Workflows
- `POST /api/workflows` - Create workflow (authenticated)
- `GET /api/workflows` - List workflows (authenticated)
- `GET /api/workflows/:id` - Get workflow (authenticated)
- `PUT /api/workflows/:id` - Update workflow (authenticated)
- `POST /api/workflows/:id/test` - Test run workflow (authenticated)

### Deployments
- `POST /api/deployments` - Create deployment (authenticated)
- `GET /api/deployments` - List deployments (authenticated)
- `POST /api/deployments/:id/promote` - Promote deployment (authenticated)
- `POST /api/deployments/:id/rollout` - Update rollout percentage (authenticated)
- `POST /api/deployments/:id/execute` - Execute workflow (authenticated)

## Configuration

Environment variables (set in `.env` or system environment):

- `PORT` - Server port (default: 3000)
- `DATABASE_URL` - PostgreSQL JDBC URL
- `JWT_SECRET` - JWT signing secret (must match Node.js backend for compatibility)
- `CAMUNDA_REST_URL` - Camunda REST API URL
- `NODE_ENV` - Environment mode (development/production)
- `CORS_ORIGINS` - Allowed CORS origins (comma-separated)

## Architecture

- **Entities**: JPA entities for database tables (Tenant, Workflow, Deployment, WorkflowExecution)
- **Repositories**: Spring Data JPA repositories for data access
- **Services**: Business logic layer with tenant isolation
- **Controllers**: REST API endpoints
- **Security**: JWT authentication with Spring Security
- **Configuration**: Spring Boot auto-configuration with custom configs

## Testing

Run all tests:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=ApplicationTest
```

## Migration Notes

This Java backend is fully compatible with the Node.js backend:

- Uses the same database schema (no migrations needed)
- JWT tokens are interoperable (same secret and algorithm)
- Password hashes are compatible (BCrypt with work factor 10)
- API contracts are identical
- JSON serialization is compatible

Both backends can run simultaneously during migration, allowing gradual rollout.

## Development

### Project Structure

```
src/main/java/com/workflowsaas/
├── config/          # Spring configuration
├── controller/      # REST controllers
├── dto/            # Data transfer objects
├── entity/         # JPA entities
├── exception/      # Exception classes
├── repository/     # Spring Data repositories
├── security/       # Security components
└── service/        # Business logic
```

### Adding New Features

1. Create entity in `entity/` package
2. Create repository in `repository/` package
3. Create service in `service/` package
4. Create controller in `controller/` package
5. Add DTOs in `dto/` package
6. Write tests in `src/test/java/`

## Troubleshooting

### Database Connection Issues

Ensure PostgreSQL is running and DATABASE_URL is correct:

```bash
docker-compose ps
```

### Port Already in Use

Change the PORT in `.env` file or stop the Node.js backend.

### JWT Token Issues

Ensure JWT_SECRET matches between Node.js and Java backends for token compatibility.

## Production Deployment

1. Build the JAR:
   ```bash
   mvn clean package -DskipTests
   ```

2. Set environment variables in production

3. Run with production profile:
   ```bash
   java -jar -Dspring.profiles.active=production target/workflow-saas-1.0.0.jar
   ```

4. Use a reverse proxy (nginx) for SSL termination

5. Set up monitoring and logging

## License

Proprietary
