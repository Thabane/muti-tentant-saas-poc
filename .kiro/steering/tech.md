# Technology Stack

## Backend (Java)

- Java 17
- Spring Boot 3.1.5 (compatible with Camunda 7.20.0)
- Spring Data JPA with Hibernate
- Spring Security with JWT
- Maven 3.9+ for build management
- Camunda BPM Platform 7.20.0 (embedded)
- PostgreSQL 14+ with Liquibase migrations
- Lombok for boilerplate reduction
- jqwik 1.8.2 for property-based testing

## Frontend

- React 18.2.0
- Vite 5.0.8 (build tool)
- React Router 6.20.1
- Axios for HTTP requests
- bpmn-js 17.0.2 (BPMN visual editor)
- dmn-js 16.0.0 (DMN decision tables)

## Infrastructure

- Docker Compose for local development
- PostgreSQL 15 (Alpine)
- Camunda BPM Platform 7.20.0 (Docker)

## Common Commands

### Backend (from java-backend/)

```bash
# Run application
mvn spring-boot:run

# Build JAR
mvn clean package

# Run tests
mvn test

# Run specific test
mvn test -Dtest=ClassName

# Skip tests during build
mvn clean package -DskipTests
```

### Frontend (from frontend/)

```bash
# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Infrastructure (from project root)

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Restart specific service
docker-compose restart postgres
```

## Environment Configuration

Backend uses `.env` file in `java-backend/` directory:
- PORT (default: 3000)
- DATABASE_URL (JDBC format)
- JWT_SECRET (must match Node.js backend for compatibility)
- CAMUNDA_REST_URL
- NODE_ENV (development/production)
- CORS_ORIGINS (comma-separated)

## Testing Frameworks

- JUnit 5 for unit tests
- Spring Boot Test for integration tests
- jqwik for property-based testing
- H2 in-memory database for test isolation
- Mockito for mocking
