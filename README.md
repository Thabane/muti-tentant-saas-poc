# Multi-Tenant Workflow SaaS Platform

A multi-tenant SaaS system enabling tenants to design, test, and deploy custom workflows using BPMN and DMN standards.

## Architecture Overview

```
├── backend/              # Node.js/Express API
│   ├── src/
│   │   ├── models/      # Data models (Tenant, Workflow, Deployment)
│   │   ├── routes/      # API endpoints
│   │   ├── services/    # Business logic
│   │   └── middleware/  # Auth, tenant isolation
├── frontend/            # React UI with bpmn.io integration
│   ├── src/
│   │   ├── components/  # UI components
│   │   ├── pages/       # Dashboard, Designer, Deploy
│   │   └── services/    # API client
├── workflows/           # Tenant workflow storage (isolated)
└── docker-compose.yml   # Local development setup
```

## Key Features

- **Tenant Isolation**: Database-level isolation with tenant-specific schemas
- **Visual Workflow Designer**: bpmn.io integration for BPMN/DMN editing
- **Environment Management**: Test → Non-Prod → Production promotion
- **Selective Rollouts**: Feature flags and gradual deployment
- **Simulation Engine**: Test-run workflows before deployment

## Tech Stack

- Backend: Node.js, Express, PostgreSQL, Camunda Platform 7
- Frontend: React, bpmn-js, dmn-js
- Orchestration: Camunda BPM 7.20 (workflow engine with REST API)
- Auth: JWT with tenant context
