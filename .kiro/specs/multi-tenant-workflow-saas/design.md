# Design Document: Multi-Tenant Workflow SaaS Platform

## Overview

The Multi-Tenant Workflow SaaS Platform is a web-based system that enables organizations to design, test, and deploy business workflows using industry-standard BPMN and DMN notations. The platform provides complete tenant isolation, visual workflow design capabilities powered by bpmn.io, and a sophisticated multi-environment deployment strategy with selective rollout controls.

### Key Design Principles

1. **Tenant Isolation**: All data and workflow executions are strictly isolated by tenant identifier at both the application and workflow engine levels
2. **Progressive Deployment**: Three-tier environment strategy (Test → Non-Production → Production) with controlled promotion
3. **Visual-First Design**: Leverage bpmn.io libraries for intuitive drag-and-drop workflow creation
4. **Standards-Based**: Full compliance with BPMN 2.0 and DMN 1.3 specifications
5. **Camunda Integration**: Delegate workflow execution to Camunda Platform 7 for reliability and scalability

### Technology Stack

- **Frontend**: React 18, React Router, bpmn-js, dmn-js, Axios
- **Backend**: Node.js 18+, Express 4, PostgreSQL 14+
- **Workflow Engine**: Camunda Platform 7.20 (REST API integration)
- **Authentication**: JWT with tenant context
- **Infrastructure**: Docker Compose for local development

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (React)                      │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │Dashboard │  │ Designer │  │Deployment│  │Onboarding│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│         │              │              │              │       │
│         └──────────────┴──────────────┴──────────────┘       │
│                        │ (Axios HTTP)                        │
└────────────────────────┼─────────────────────────────────────┘
                         │
┌────────────────────────┼─────────────────────────────────────┐
│                        │  Backend (Express)                   │
│                        ▼                                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │         Authentication Middleware (JWT)               │   │
│  │         Tenant Context Extraction                     │   │
│  └──────────────────────────────────────────────────────┘   │
│                        │                                      │
│  ┌─────────────┬───────┴────────┬──────────────┐           │
│  │   Tenant    │   Workflow     │  Deployment  │           │
│  │   Routes    │   Routes       │  Routes      │           │
│  └──────┬──────┴────────┬───────┴──────┬───────┘           │
│         │               │              │                     │
│  ┌──────▼──────┐ ┌──────▼──────┐ ┌────▼─────────┐         │
│  │   Tenant    │ │  Workflow   │ │  Deployment  │         │
│  │   Service   │ │  Service    │ │  Service     │         │
│  └──────┬──────┘ └──────┬──────┘ └────┬─────────┘         │
│         │               │              │                     │
│         └───────────────┴──────────────┘                     │
│                        │                                      │
│  ┌─────────────────────▼──────────────────────────────┐    │
│  │              PostgreSQL Database                     │    │
│  │  ┌─────────┐ ┌──────────┐ ┌────────────┐          │    │
│  │  │ tenants │ │workflows │ │deployments │          │    │
│  │  └─────────┘ └──────────┘ └────────────┘          │    │
│  └──────────────────────────────────────────────────────┘    │
│                                                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │         Camunda Integration Service                  │    │
│  │  - Deploy BPMN/DMN to Camunda                       │    │
│  │  - Start Process Instances                          │    │
│  │  - Variable Type Conversion                         │    │
│  └────────────────────┬─────────────────────────────────┘    │
└─────────────────────────┼───────────────────────────────────┘
                          │ (REST API)
┌─────────────────────────▼───────────────────────────────────┐
│              Camunda Platform 7 (Workflow Engine)            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│  │   Process    │  │   Execution  │  │   History    │     │
│  │  Definitions │  │   Engine     │  │   Service    │     │
│  └──────────────┘  └──────────────┘  └──────────────┘     │
│                   (Multi-tenant isolation)                   │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

#### Workflow Design Flow
1. User opens Visual Designer in browser
2. bpmn-js library renders BPMN diagram
3. User modifies diagram using drag-and-drop
4. On save, frontend sends BPMN XML to backend
5. Backend validates tenant ownership and stores XML in database

#### Test Execution Flow
1. User provides JSON input in designer
2. Frontend sends test request to backend
3. Backend simulates execution (no Camunda interaction)
4. Backend returns mock results and stores test history
5. Frontend displays results in test panel

#### Deployment Flow
1. User selects workflow and target environment
2. Frontend sends deployment request with rollout percentage
3. Backend retrieves workflow BPMN XML
4. Backend calls Camunda REST API with tenant-id
5. Camunda deploys process definition
6. Backend stores deployment record with Camunda deployment ID
7. Frontend displays deployment status

#### Workflow Execution Flow
1. User triggers execution with JSON input
2. Backend retrieves deployment and Camunda deployment ID
3. Backend converts JSON to Camunda variable format
4. Backend calls Camunda start process endpoint with tenant-id
5. Camunda creates process instance
6. Backend returns instance ID to frontend

## Components and Interfaces

### Frontend Components

#### Dashboard Component
- **Purpose**: Display tenant overview with workflow and deployment statistics
- **State**: workflows[], deployments[], tenant
- **Key Functions**:
  - `loadData()`: Fetch workflows, deployments, and tenant info
  - `handleLogout()`: Clear token and redirect to login
- **API Calls**: GET /api/workflows, GET /api/deployments, GET /api/tenants/me

#### WorkflowDesigner Component
- **Purpose**: Visual BPMN/DMN editor with test capabilities
- **State**: workflow, name, testInput, testResult, modelerRef, availableDmnWorkflows
- **Key Functions**:
  - `loadWorkflow()`: Load existing workflow BPMN XML
  - `handleSave()`: Save BPMN XML to backend
  - `handleTest()`: Execute test run with input data
  - `loadAvailableDmnWorkflows()`: Fetch DMN workflows for Business Rule Task configuration
  - `handleBusinessRuleTaskConfig()`: Configure DMN reference for Business Rule Task
- **Libraries**: bpmn-js Modeler for BPMN editing, dmn-js Modeler for DMN editing
- **API Calls**: GET /api/workflows/:id, POST /api/workflows, PUT /api/workflows/:id, POST /api/workflows/:id/test, GET /api/workflows?type=dmn

#### Deployments Component
- **Purpose**: Manage deployments across environments
- **State**: deployments[], workflows[], selectedWorkflow, environment, rollout
- **Key Functions**:
  - `handleDeploy()`: Create new deployment
  - `handlePromote()`: Promote deployment to higher environment
  - `handleRolloutUpdate()`: Update rollout percentage
  - `handleExecute()`: Execute workflow in deployed environment
- **API Calls**: GET /api/deployments, POST /api/deployments, POST /api/deployments/:id/promote, POST /api/deployments/:id/rollout, POST /api/deployments/:id/execute

#### Onboarding Component
- **Purpose**: Guide new tenants through platform features
- **State**: step (1-3)
- **Key Functions**:
  - `handleComplete()`: Mark onboarding as complete
- **API Calls**: PATCH /api/tenants/onboarding

### Backend Services

#### TenantService
- **Purpose**: Manage tenant registration, authentication, and configuration
- **Methods**:
  - `register(name, email, password)`: Create new tenant with hashed password
  - `login(email, password)`: Validate credentials and return JWT
  - `getById(tenantId)`: Retrieve tenant information
  - `updateFeatures(tenantId, features)`: Update tenant feature flags
  - `completeOnboarding(tenantId)`: Mark tenant as onboarded
- **Database Tables**: tenants

#### WorkflowService
- **Purpose**: Manage workflow CRUD operations
- **Methods**:
  - `create(tenantId, name, type, bpmnXml)`: Create new workflow
  - `getAll(tenantId)`: List all workflows for tenant
  - `getById(tenantId, workflowId)`: Retrieve specific workflow
  - `update(tenantId, workflowId, updates)`: Update workflow XML or metadata
  - `delete(tenantId, workflowId)`: Delete workflow
  - `testRun(tenantId, workflowId, inputData)`: Simulate workflow execution
  - `getDmnWorkflows(tenantId)`: List all DMN workflows for tenant
  - `extractDmnReferences(bpmnXml)`: Parse BPMN XML to extract DMN decision references
  - `validateDmnDependencies(tenantId, dmnReferences)`: Verify all referenced DMN workflows exist
  - `checkDmnUsage(tenantId, dmnWorkflowId)`: Find BPMN workflows that reference a DMN workflow
- **Database Tables**: workflows, workflow_executions

#### DeploymentService
- **Purpose**: Manage deployments and environment promotions
- **Methods**:
  - `create(tenantId, workflowId, environment, rolloutPercentage)`: Deploy workflow to environment
  - `getAll(tenantId, environmentFilter)`: List deployments for tenant
  - `promote(tenantId, deploymentId, targetEnvironment, rolloutPercentage)`: Promote to higher environment
  - `updateRollout(tenantId, deploymentId, newPercentage)`: Update rollout percentage
  - `execute(tenantId, deploymentId, inputData)`: Start process instance in Camunda
  - `deployWithDependencies(tenantId, workflowId, environment, rolloutPercentage)`: Deploy BPMN with all DMN dependencies
- **Database Tables**: deployments

#### CamundaService
- **Purpose**: Interface with Camunda Platform 7 REST API
- **Methods**:
  - `deployProcess(tenantId, bpmnXml, deploymentName)`: Deploy BPMN to Camunda
  - `deployDmn(tenantId, dmnXml, deploymentName)`: Deploy DMN to Camunda
  - `deployMultiple(tenantId, resources, deploymentName)`: Deploy multiple resources (BPMN + DMN) in single deployment
  - `startProcessInstance(tenantId, processDefinitionKey, variables)`: Start process instance
  - `convertVariables(jsonData)`: Convert JSON to Camunda variable format
  - `getProcessInstance(instanceId)`: Query process instance status
- **External Dependency**: Camunda REST API at http://localhost:8080/engine-rest

### BPMN-DMN Integration

#### DMN Reference Storage in BPMN

Business Rule Tasks in BPMN reference DMN decisions using the `camunda:decisionRef` attribute:

```xml
<bpmn:businessRuleTask id="Task_DecideDiscount" name="Calculate Discount" 
                       camunda:decisionRef="decision_discount_calculation">
  <bpmn:incoming>Flow_1</bpmn:incoming>
  <bpmn:outgoing>Flow_2</bpmn:outgoing>
</bpmn:businessRuleTask>
```

The `decisionRef` value corresponds to the `id` attribute of a DMN decision in the DMN XML:

```xml
<decision id="decision_discount_calculation" name="Discount Calculation">
  <decisionTable id="DecisionTable_1">
    <!-- decision table content -->
  </decisionTable>
</decision>
```

#### DMN Reference Extraction

The `extractDmnReferences` method parses BPMN XML to find all Business Rule Tasks with `camunda:decisionRef` attributes:

```javascript
function extractDmnReferences(bpmnXml) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(bpmnXml, 'text/xml');
  const businessRuleTasks = doc.getElementsByTagName('bpmn:businessRuleTask');
  
  const references = [];
  for (let task of businessRuleTasks) {
    const decisionRef = task.getAttribute('camunda:decisionRef');
    if (decisionRef) {
      references.push(decisionRef);
    }
  }
  
  return [...new Set(references)]; // Remove duplicates
}
```

#### DMN Dependency Validation

Before deploying a BPMN workflow, the system validates that all referenced DMN workflows exist:

```javascript
async function validateDmnDependencies(tenantId, dmnReferences) {
  const dmnWorkflows = await workflowService.getDmnWorkflows(tenantId);
  const availableDecisionKeys = dmnWorkflows.map(w => extractDecisionKey(w.dmn_xml));
  
  const missingReferences = dmnReferences.filter(ref => 
    !availableDecisionKeys.includes(ref)
  );
  
  if (missingReferences.length > 0) {
    throw new ValidationError(
      `Missing DMN dependencies: ${missingReferences.join(', ')}`
    );
  }
  
  return dmnWorkflows.filter(w => 
    dmnReferences.includes(extractDecisionKey(w.dmn_xml))
  );
}
```

#### Multi-Resource Deployment

When deploying a BPMN workflow with DMN dependencies, all resources are deployed together in a single Camunda deployment:

```javascript
async function deployWithDependencies(tenantId, workflowId, environment, rolloutPercentage) {
  // Get BPMN workflow
  const bpmnWorkflow = await workflowService.getById(tenantId, workflowId);
  
  // Extract DMN references
  const dmnReferences = extractDmnReferences(bpmnWorkflow.bpmn_xml);
  
  // Validate and get DMN workflows
  const dmnWorkflows = await validateDmnDependencies(tenantId, dmnReferences);
  
  // Prepare resources for deployment
  const resources = [
    { name: `${bpmnWorkflow.name}.bpmn`, content: bpmnWorkflow.bpmn_xml, type: 'bpmn' }
  ];
  
  for (let dmn of dmnWorkflows) {
    resources.push({
      name: `${dmn.name}.dmn`,
      content: dmn.dmn_xml,
      type: 'dmn'
    });
  }
  
  // Deploy all resources together
  const camundaDeploymentId = await camundaService.deployMultiple(
    tenantId,
    resources,
    `${bpmnWorkflow.name}-${Date.now()}`
  );
  
  // Create deployment record
  return await createDeploymentRecord(
    tenantId,
    workflowId,
    environment,
    rolloutPercentage,
    camundaDeploymentId,
    { dmnDependencies: dmnReferences }
  );
}
```

#### Business Rule Task Configuration UI

The WorkflowDesigner component provides a properties panel for configuring Business Rule Tasks:

1. User selects a Business Rule Task in the BPMN diagram
2. Properties panel displays a dropdown of available DMN workflows
3. User selects a DMN workflow from the dropdown
4. System extracts the decision key from the selected DMN workflow
5. System updates the BPMN XML with `camunda:decisionRef` attribute
6. User saves the workflow with the DMN reference

#### DMN Deletion Protection

When a user attempts to delete a DMN workflow, the system checks for dependent BPMN workflows:

```javascript
async function checkDmnUsage(tenantId, dmnWorkflowId) {
  const dmnWorkflow = await workflowService.getById(tenantId, dmnWorkflowId);
  const decisionKey = extractDecisionKey(dmnWorkflow.dmn_xml);
  
  const allBpmnWorkflows = await workflowService.getAll(tenantId, { type: 'bpmn' });
  const dependentWorkflows = [];
  
  for (let bpmn of allBpmnWorkflows) {
    const references = extractDmnReferences(bpmn.bpmn_xml);
    if (references.includes(decisionKey)) {
      dependentWorkflows.push(bpmn.name);
    }
  }
  
  return dependentWorkflows;
}
```

If dependent workflows exist, deletion is prevented:

```javascript
async function delete(tenantId, workflowId) {
  const workflow = await getById(tenantId, workflowId);
  
  if (workflow.type === 'dmn') {
    const dependentWorkflows = await checkDmnUsage(tenantId, workflowId);
    if (dependentWorkflows.length > 0) {
      throw new ValidationError(
        `Cannot delete DMN workflow. Referenced by: ${dependentWorkflows.join(', ')}`
      );
    }
  }
  
  // Proceed with deletion
  await db.query('DELETE FROM workflows WHERE id = $1 AND tenant_id = $2', 
    [workflowId, tenantId]);
}
```

### API Endpoints

#### Tenant Endpoints
- `POST /api/tenants/register`: Register new tenant
  - Body: `{ name, email, password }`
  - Response: `{ id, name, email, token }`
- `POST /api/tenants/login`: Authenticate tenant
  - Body: `{ email, password }`
  - Response: `{ token, tenant: { id, name, email } }`
- `GET /api/tenants/me`: Get current tenant (requires auth)
  - Response: `{ id, name, email, features, onboarded }`
- `PATCH /api/tenants/onboarding`: Complete onboarding (requires auth)
  - Response: `{ success: true }`

#### Workflow Endpoints
- `POST /api/workflows`: Create workflow (requires auth)
  - Body: `{ name, type, bpmn_xml }`
  - Response: `{ id, name, type, version, created_at }`
- `GET /api/workflows`: List workflows (requires auth)
  - Query: `?type=dmn` (optional filter by type)
  - Response: `[{ id, name, type, status, version, created_at }]`
- `GET /api/workflows/:id`: Get workflow (requires auth)
  - Response: `{ id, name, type, bpmn_xml, version, created_at }`
- `PUT /api/workflows/:id`: Update workflow (requires auth)
  - Body: `{ name?, bpmn_xml? }`
  - Response: `{ id, name, type, version, updated_at }`
- `DELETE /api/workflows/:id`: Delete workflow (requires auth)
  - Response: `{ success: true }` or error if DMN has dependencies
- `POST /api/workflows/:id/test`: Test run workflow (requires auth)
  - Body: `{ inputData: {} }`
  - Response: `{ status, output, execution_id }`
- `GET /api/workflows/:id/dependencies`: Get DMN dependencies for BPMN workflow (requires auth)
  - Response: `{ dmnReferences: ['decision_key_1', 'decision_key_2'], dmnWorkflows: [{ id, name, decision_key }] }`
- `GET /api/workflows/:id/usage`: Check if DMN workflow is used by BPMN workflows (requires auth)
  - Response: `{ isUsed: true, dependentWorkflows: [{ id, name }] }`

#### Deployment Endpoints
- `POST /api/deployments`: Create deployment (requires auth)
  - Body: `{ workflowId, environment, rolloutPercentage }`
  - Response: `{ id, workflow_id, environment, status, rollout_percentage, deployed_at }`
- `GET /api/deployments`: List deployments (requires auth)
  - Query: `?environment=production`
  - Response: `[{ id, workflow_name, environment, status, rollout_percentage, deployed_at }]`
- `POST /api/deployments/:id/promote`: Promote deployment (requires auth)
  - Body: `{ targetEnvironment, rolloutPercentage }`
  - Response: `{ id, environment, status, deployed_at }`
- `POST /api/deployments/:id/rollout`: Update rollout (requires auth)
  - Body: `{ rolloutPercentage }`
  - Response: `{ id, rollout_percentage }`
- `POST /api/deployments/:id/execute`: Execute workflow (requires auth)
  - Body: `{ inputData: {} }`
  - Response: `{ processInstanceId, status }`

### Middleware

#### Authentication Middleware
```javascript
function authenticateToken(req, res, next) {
  const token = req.headers.authorization?.split(' ')[1];
  if (!token) return res.status(401).json({ error: 'No token provided' });
  
  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) return res.status(403).json({ error: 'Invalid token' });
    req.tenantId = decoded.tenantId;
    req.userId = decoded.userId;
    next();
  });
}
```

#### Tenant Isolation Middleware
```javascript
function ensureTenantContext(req, res, next) {
  if (!req.tenantId) {
    return res.status(401).json({ error: 'Tenant context required' });
  }
  next();
}
```

## Data Models

### Database Schema

#### tenants Table
```sql
CREATE TABLE tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  features JSONB DEFAULT '{}',
  onboarded BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_tenants_email ON tenants(email);
```

**Fields**:
- `id`: Unique tenant identifier (UUID)
- `name`: Organization name
- `email`: Primary contact email (unique)
- `password_hash`: bcrypt hashed password
- `features`: JSON object with feature flags (dmn_enabled, max_workflows, max_deployments)
- `onboarded`: Whether tenant completed onboarding
- `created_at`: Registration timestamp
- `updated_at`: Last modification timestamp

#### workflows Table
```sql
CREATE TABLE workflows (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  type VARCHAR(50) NOT NULL CHECK (type IN ('bpmn', 'dmn')),
  bpmn_xml TEXT,
  dmn_xml TEXT,
  version INTEGER DEFAULT 1,
  status VARCHAR(50) DEFAULT 'draft',
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_workflows_tenant ON workflows(tenant_id);
CREATE INDEX idx_workflows_type ON workflows(type);
```

**Fields**:
- `id`: Unique workflow identifier (UUID)
- `tenant_id`: Foreign key to tenants table
- `name`: Workflow name
- `type`: Workflow type ('bpmn' or 'dmn')
- `bpmn_xml`: BPMN 2.0 XML definition
- `dmn_xml`: DMN 1.3 XML definition
- `version`: Version number (incremented on update)
- `status`: Workflow status ('draft', 'active', 'archived')
- `created_at`: Creation timestamp
- `updated_at`: Last modification timestamp

#### deployments Table
```sql
CREATE TABLE deployments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
  environment VARCHAR(50) NOT NULL CHECK (environment IN ('test', 'non-prod', 'production')),
  status VARCHAR(50) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'failed')),
  rollout_percentage INTEGER DEFAULT 100 CHECK (rollout_percentage >= 0 AND rollout_percentage <= 100),
  camunda_deployment_id VARCHAR(255),
  metadata JSONB DEFAULT '{}',
  deployed_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_deployments_tenant ON deployments(tenant_id);
CREATE INDEX idx_deployments_workflow ON deployments(workflow_id);
CREATE INDEX idx_deployments_environment ON deployments(environment);
```

**Fields**:
- `id`: Unique deployment identifier (UUID)
- `tenant_id`: Foreign key to tenants table
- `workflow_id`: Foreign key to workflows table
- `environment`: Target environment ('test', 'non-prod', 'production')
- `status`: Deployment status ('active', 'inactive', 'failed')
- `rollout_percentage`: Percentage of traffic receiving this deployment (0-100)
- `camunda_deployment_id`: Camunda's deployment identifier (null for test environment)
- `metadata`: Additional deployment information (process definition key, version, dmnDependencies array)
- `deployed_at`: Deployment timestamp

#### workflow_executions Table
```sql
CREATE TABLE workflow_executions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
  workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
  deployment_id UUID REFERENCES deployments(id) ON DELETE SET NULL,
  execution_type VARCHAR(50) NOT NULL CHECK (execution_type IN ('test', 'real')),
  input_data JSONB,
  output_data JSONB,
  status VARCHAR(50) DEFAULT 'running' CHECK (status IN ('running', 'completed', 'failed')),
  camunda_instance_id VARCHAR(255),
  error_message TEXT,
  started_at TIMESTAMP DEFAULT NOW(),
  completed_at TIMESTAMP
);

CREATE INDEX idx_executions_tenant ON workflow_executions(tenant_id);
CREATE INDEX idx_executions_workflow ON workflow_executions(workflow_id);
CREATE INDEX idx_executions_status ON workflow_executions(status);
```

**Fields**:
- `id`: Unique execution identifier (UUID)
- `tenant_id`: Foreign key to tenants table
- `workflow_id`: Foreign key to workflows table
- `deployment_id`: Foreign key to deployments table (null for test runs)
- `execution_type`: Type of execution ('test' or 'real')
- `input_data`: JSON input variables
- `output_data`: JSON output variables
- `status`: Execution status ('running', 'completed', 'failed')
- `camunda_instance_id`: Camunda process instance ID (null for test runs)
- `error_message`: Error details if execution failed
- `started_at`: Execution start timestamp
- `completed_at`: Execution completion timestamp

### Data Relationships

```
tenants (1) ──< (N) workflows
tenants (1) ──< (N) deployments
tenants (1) ──< (N) workflow_executions
workflows (1) ──< (N) deployments
workflows (1) ──< (N) workflow_executions
deployments (1) ──< (N) workflow_executions
```

### Camunda Data Structures

#### Variable Format
Camunda requires variables in a specific format:
```javascript
{
  "variableName": {
    "value": actualValue,
    "type": "String" | "Integer" | "Double" | "Boolean" | "Json"
  }
}
```

#### Type Conversion Rules
- JavaScript `string` → Camunda `String`
- JavaScript `number` (integer) → Camunda `Integer`
- JavaScript `number` (float) → Camunda `Double`
- JavaScript `boolean` → Camunda `Boolean`
- JavaScript `object` or `array` → Camunda `Json`
- JavaScript `null` → Camunda `Null`

## Correctness Properties


*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Tenant Registration Creates Unique Isolated Accounts
*For any* valid registration data (organization name, email, password), creating a tenant account should result in a unique tenant identifier and the ability to perform tenant-scoped operations without interference from other tenants.
**Validates: Requirements 1.1, 1.2**

### Property 2: JWT Tokens Contain Tenant Context
*For any* successful login with valid credentials, the issued JWT token should contain the tenant identifier, and decoding that token should yield the same tenant identifier.
**Validates: Requirements 1.3**

### Property 3: Duplicate Email Registration Fails
*For any* email address that already exists in the system, attempting to register a new tenant with that email should fail with an error message.
**Validates: Requirements 1.5**

### Property 4: Onboarding Completion Updates Tenant State
*For any* tenant that completes the onboarding flow, the tenant record should be marked as onboarded, and subsequent checks should reflect this completed state.
**Validates: Requirements 2.4, 2.5**

### Property 5: Workflow Save and Load Round Trip
*For any* valid BPMN or DMN XML, saving a workflow and then loading it should return XML that is semantically equivalent to the original.
**Validates: Requirements 3.4, 3.5, 4.4**

### Property 6: New Workflows Have Initial Structure
*For any* newly created BPMN workflow, the initial BPMN XML should contain a start event, and for any newly created DMN workflow, the initial DMN XML should contain a decision table structure.
**Validates: Requirements 3.2, 4.2**

### Property 7: Test Runs Do Not Deploy to Camunda
*For any* test run execution, the system should complete without creating a Camunda deployment or process instance, and should return execution status and output.
**Validates: Requirements 5.1, 5.2**

### Property 8: Test Run History Persistence
*For any* test run execution, after completion, querying the execution history should return a record containing the input data, output data, status, and timestamp.
**Validates: Requirements 5.4, 5.5**

### Property 9: Invalid Input Produces Error Responses
*For any* invalid input (malformed JSON, invalid BPMN XML, out-of-range rollout percentage), the system should reject the request and return a descriptive error message without processing the invalid data.
**Validates: Requirements 5.3, 17.1, 17.3**

### Property 10: Non-Test Deployments Create Camunda Deployments
*For any* deployment to Non-Production or Production environment, the system should call the Camunda REST API, and upon success, store a deployment record with a Camunda deployment identifier.
**Validates: Requirements 6.2, 6.4**

### Property 11: Camunda Deployments Include Tenant Identifier
*For any* deployment to Camunda (Non-Production or Production), the Camunda API call should include the tenant identifier to ensure process isolation.
**Validates: Requirements 6.3, 9.3, 13.2, 15.3**

### Property 12: Failed Camunda Deployments Do Not Create Records
*For any* deployment attempt that fails at the Camunda level, the system should return an error message and should not create a deployment record in the database.
**Validates: Requirements 6.5**

### Property 13: Rollout Percentage Validation and Storage
*For any* deployment creation or update, the system should accept rollout percentages between 0 and 100 (inclusive), reject values outside this range, and immediately reflect accepted values in subsequent queries.
**Validates: Requirements 7.1, 7.3, 7.4**

### Property 14: Multiple Deployments Per Workflow
*For any* workflow, the system should allow creating multiple deployments to different environments, each with independent rollout percentages and statuses.
**Validates: Requirements 7.5**

### Property 15: Environment Promotion Creates New Deployment
*For any* deployment in Test or Non-Production, promoting to the next environment should create a new deployment record in the target environment with the same workflow and specified rollout percentage.
**Validates: Requirements 8.1, 8.2, 8.3**

### Property 16: Production Promotion Rejection
*For any* deployment in Production environment, attempting to promote should fail with an error indicating that promotion from Production is not allowed.
**Validates: Requirements 8.4**

### Property 17: Successful Promotion Deploys to Camunda
*For any* successful promotion to Non-Production or Production, the system should create a Camunda deployment in the target environment.
**Validates: Requirements 8.5**

### Property 18: Workflow Execution Creates Camunda Process Instance
*For any* workflow execution in Non-Production or Production, the system should start a process instance in Camunda and return the Camunda instance identifier.
**Validates: Requirements 9.1, 9.4**

### Property 19: Variable Type Conversion to Camunda Format
*For any* JSON input data, the system should convert JavaScript types to Camunda variable format according to the type mapping rules (string→String, number→Integer/Double, boolean→Boolean, object/array→Json).
**Validates: Requirements 9.2, 15.4**

### Property 20: Tenant Isolation in API Queries
*For any* authenticated API request, all database queries should be scoped to the tenant identifier from the JWT token, and should return only resources belonging to that tenant.
**Validates: Requirements 1.4, 11.2, 12.1, 13.1**

### Property 21: Cross-Tenant Access Prevention
*For any* attempt to access a resource (workflow, deployment, execution) belonging to a different tenant, the system should reject the request with an authorization error.
**Validates: Requirements 11.3, 13.4, 13.5**

### Property 22: Workflow Ownership Verification for Mutations
*For any* workflow update or delete operation, the system should verify that the workflow belongs to the requesting tenant before performing the operation, and should reject operations on workflows owned by other tenants.
**Validates: Requirements 11.4, 11.5**

### Property 23: Unauthenticated Requests Rejection
*For any* API request without a valid JWT token, the system should reject the request with an authentication error.
**Validates: Requirements 13.3**

### Property 24: Workflow Version Increment on Update
*For any* workflow update operation, the system should increment the version number, and the new version should be greater than the previous version.
**Validates: Requirements 14.1**

### Property 25: Deployment Records Workflow Version
*For any* deployment, the system should record which version of the workflow was deployed, and this version should be retrievable from the deployment metadata.
**Validates: Requirements 14.3**

### Property 26: Workflow Version History Completeness
*For any* workflow with multiple updates, querying the version history should return all versions with their timestamps in chronological order.
**Validates: Requirements 14.4, 14.5**

### Property 27: Camunda Deployment Format Compliance
*For any* deployment to Camunda, the system should send BPMN XML as multipart form data with deployment-name, tenant-id, and the BPMN file.
**Validates: Requirements 15.2**

### Property 28: Camunda Error Propagation
*For any* Camunda operation that returns an error, the system should parse the Camunda error response and return a user-friendly error message to the client.
**Validates: Requirements 15.5, 17.2**

### Property 29: Tenant Feature Configuration Initialization
*For any* newly created tenant, the system should initialize a features configuration object with default values for DMN support, maximum workflows, maximum deployments, and API access.
**Validates: Requirements 16.1, 16.2**

### Property 30: Feature Flag Enforcement
*For any* tenant with a disabled feature, attempting to use that feature should fail with an error indicating the feature is not available.
**Validates: Requirements 16.4**

### Property 31: Error Logging Completeness
*For any* error that occurs in the system, an error log entry should be created containing the timestamp, tenant identifier (if available), error message, and stack trace.
**Validates: Requirements 17.5**

### Property 32: Execution History Completeness
*For any* workflow, querying the execution history should return all test runs and real executions for that workflow, each with input data, output data, status, and timestamps.
**Validates: Requirements 18.1, 18.2, 18.4**

### Property 33: Execution History Filtering
*For any* execution history query with date range or status filters, the results should include only executions matching the filter criteria.
**Validates: Requirements 18.5**

### Property 34: Tenant Configuration Updates
*For any* valid tenant configuration update (organization name, notification preferences, default rollout percentage), the system should update the tenant record and subsequent queries should reflect the new values.
**Validates: Requirements 19.1, 19.2, 19.3, 19.4**

### Property 35: Deployment Status Lifecycle
*For any* deployment, the status should be set to 'active' upon successful creation, 'failed' if Camunda deployment fails, and 'inactive' when explicitly deactivated by a user.
**Validates: Requirements 20.2, 20.3, 20.4**

### Property 36: Dashboard Data Completeness
*For any* tenant dashboard request, the response should include accurate counts of total workflows, active deployments by environment, and lists of workflows and recent deployments with all required fields (name, type, environment, status, rollout percentage, timestamps).
**Validates: Requirements 10.1, 10.2, 10.3, 10.4, 12.2**

### Property 37: Deployment Environment Filtering
*For any* deployment list query with an environment filter, the results should include only deployments in the specified environment.
**Validates: Requirements 12.3**

### Property 38: Deployment Count Aggregation
*For any* tenant, the count of deployments per environment should equal the number of deployment records with that environment value.
**Validates: Requirements 12.4**

### Property 39: DMN Workflow Listing for Business Rule Tasks
*For any* tenant, when listing DMN workflows for Business Rule Task configuration, the system should return only DMN workflows belonging to that tenant.
**Validates: Requirements 21.6**

### Property 40: DMN Reference Storage in BPMN
*For any* Business Rule Task configured with a DMN reference, saving and loading the BPMN workflow should preserve the DMN decision reference in the BPMN XML.
**Validates: Requirements 21.2**

### Property 41: DMN Dependency Extraction
*For any* BPMN workflow containing Business Rule Tasks with DMN references, extracting DMN dependencies should return all unique decision keys referenced in the BPMN XML.
**Validates: Requirements 21.3**

### Property 42: DMN Dependency Validation
*For any* BPMN workflow deployment with DMN references, if all referenced DMN workflows exist for the tenant, validation should succeed; if any DMN workflow is missing, validation should fail with an error listing the missing references.
**Validates: Requirements 21.5**

### Property 43: Multi-Resource Deployment with DMN Dependencies
*For any* BPMN workflow with valid DMN dependencies deployed to Non-Production or Production, the system should deploy all referenced DMN workflows to Camunda in the same deployment as the BPMN workflow.
**Validates: Requirements 21.4**

### Property 44: DMN Deletion Protection
*For any* DMN workflow that is referenced by one or more BPMN workflows, attempting to delete the DMN workflow should fail with an error listing the dependent BPMN workflows.
**Validates: Requirements 21.8**

### Property 45: Business Rule Task Execution with DMN
*For any* BPMN process instance executing a Business Rule Task with a DMN reference, the Camunda engine should invoke the referenced DMN decision and the process should continue with the decision result.
**Validates: Requirements 21.7**

## Error Handling

### Error Categories

1. **Authentication Errors (401)**
   - Missing JWT token
   - Invalid or expired JWT token
   - Malformed authorization header

2. **Authorization Errors (403)**
   - Cross-tenant resource access attempts
   - Feature not enabled for tenant
   - Insufficient permissions

3. **Validation Errors (400)**
   - Invalid BPMN/DMN XML
   - Malformed JSON input
   - Out-of-range rollout percentage
   - Missing required fields
   - Invalid environment value

4. **Resource Not Found Errors (404)**
   - Workflow ID does not exist
   - Deployment ID does not exist
   - Tenant ID does not exist

5. **Conflict Errors (409)**
   - Duplicate email registration
   - Concurrent workflow updates

6. **External Service Errors (502)**
   - Camunda API unavailable
   - Camunda deployment failure
   - Camunda process start failure

7. **Internal Server Errors (500)**
   - Database connection failures
   - Unexpected exceptions
   - Data corruption

### Error Response Format

All errors should follow a consistent JSON structure:

```json
{
  "error": "Human-readable error message",
  "code": "ERROR_CODE",
  "details": {
    "field": "Additional context"
  }
}
```

### Error Handling Strategies

1. **Input Validation**: Validate all inputs at the API boundary before processing
2. **Graceful Degradation**: Return partial results when possible rather than complete failure
3. **Error Logging**: Log all errors with context for debugging without exposing sensitive data to clients
4. **Retry Logic**: Implement exponential backoff for transient Camunda API failures
5. **Transaction Rollback**: Ensure database consistency by rolling back failed multi-step operations
6. **User-Friendly Messages**: Translate technical errors into actionable user messages

### Specific Error Scenarios

#### BPMN XML Validation
- Parse XML to verify well-formedness
- Validate against BPMN 2.0 schema
- Check for required elements (at least one start event)
- Return specific validation errors with line numbers

#### DMN Dependency Validation
- Extract DMN references from BPMN Business Rule Tasks
- Verify all referenced DMN workflows exist for the tenant
- Return list of missing DMN references if validation fails
- Prevent deployment if any DMN dependency is missing

#### DMN Deletion Validation
- Check if DMN workflow is referenced by any BPMN workflows
- Return list of dependent BPMN workflows if references exist
- Prevent deletion if DMN workflow is in use
- Allow deletion only if no BPMN workflows reference the DMN

#### Camunda Integration Errors
- Network timeouts: Retry up to 3 times with exponential backoff
- 4xx errors: Return Camunda's error message to user
- 5xx errors: Log error and return generic "workflow engine unavailable" message
- Connection refused: Check Camunda health and return service unavailable

#### Tenant Isolation Violations
- Log security event with tenant IDs and resource IDs
- Return 403 Forbidden without revealing resource existence
- Alert platform administrators of potential security issues

#### Concurrent Modification
- Use optimistic locking with version numbers
- Return 409 Conflict with current version information
- Allow client to retry with updated version

## Testing Strategy

### Dual Testing Approach

The system requires both unit testing and property-based testing for comprehensive coverage:

- **Unit Tests**: Verify specific examples, edge cases, and error conditions
- **Property Tests**: Verify universal properties across all inputs using randomized test data

### Unit Testing Focus

Unit tests should cover:
- Specific examples demonstrating correct behavior (e.g., registering a tenant with specific data)
- Edge cases (e.g., empty workflow names, boundary rollout percentages)
- Error conditions (e.g., invalid JWT tokens, malformed XML)
- Integration points between components (e.g., service-to-database interactions)
- Mock Camunda API responses for various scenarios

### Property-Based Testing Configuration

**Library Selection**: Use `fast-check` for JavaScript/TypeScript property-based testing

**Test Configuration**:
- Minimum 100 iterations per property test
- Each property test must reference its design document property
- Tag format: `Feature: multi-tenant-workflow-saas, Property {number}: {property_text}`

**Example Property Test Structure**:
```javascript
import fc from 'fast-check';

// Feature: multi-tenant-workflow-saas, Property 1: Tenant Registration Creates Unique Isolated Accounts
test('tenant registration creates unique isolated accounts', () => {
  fc.assert(
    fc.property(
      fc.record({
        name: fc.string({ minLength: 1, maxLength: 255 }),
        email: fc.emailAddress(),
        password: fc.string({ minLength: 8 })
      }),
      async (registrationData) => {
        const tenant = await tenantService.register(
          registrationData.name,
          registrationData.email,
          registrationData.password
        );
        
        expect(tenant.id).toBeDefined();
        expect(tenant.id).toMatch(/^[0-9a-f-]{36}$/); // UUID format
        
        // Verify isolation
        const workflows = await workflowService.getAll(tenant.id);
        expect(workflows).toEqual([]);
      }
    ),
    { numRuns: 100 }
  );
});
```

### Test Data Generators

Property tests require generators for:
- **Tenant Data**: Random organization names, emails, passwords
- **Workflow Data**: Valid BPMN XML with random process structures
- **DMN Data**: Valid DMN XML with random decision tables
- **Deployment Data**: Random environment selections, rollout percentages
- **Execution Data**: Random JSON input variables with various types
- **JWT Tokens**: Valid tokens with random tenant IDs

### Integration Testing

Integration tests should verify:
- End-to-end workflows (register → create workflow → deploy → execute)
- Camunda integration with real Camunda instance
- Database transactions and rollbacks
- Multi-tenant isolation with concurrent requests
- Environment promotion flows

### Performance Testing

Performance tests should measure:
- API response times under load
- Database query performance with large datasets
- Camunda deployment and execution times
- Concurrent user scenarios
- Memory usage and leak detection

### Security Testing

Security tests should verify:
- JWT token validation and expiration
- SQL injection prevention
- Cross-tenant data access prevention
- BPMN XML injection attacks
- Rate limiting and DoS protection
