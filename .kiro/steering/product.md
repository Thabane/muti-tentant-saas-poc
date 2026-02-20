# Product Overview

Multi-tenant workflow SaaS platform that enables organizations to design, deploy, and execute BPMN workflows with visual tooling and environment-based deployment strategies.

## Core Features

- Visual BPMN workflow designer with bpmn-js
- Multi-tenant architecture with complete data isolation
- Environment-based deployment (test, non-prod, production)
- Camunda BPM Platform 7 integration for workflow execution
- JWT-based authentication and authorization
- Progressive rollout support (0-100% traffic)
- App-based workflow organization

## User Journey

1. Register and create tenant account
2. Complete onboarding
3. Create apps to organize workflows
4. Design BPMN workflows in visual editor
5. Test workflows in sandbox environment
6. Deploy to non-production for validation
7. Promote to production with controlled rollout
8. Monitor executions via Camunda Cockpit

## Technical Approach

- Backend migrated from Node.js to Java 17 + Spring Boot
- React frontend with Vite build system
- PostgreSQL for persistence with Liquibase migrations
- Camunda 7 embedded engine for workflow execution
- Docker Compose for local infrastructure
