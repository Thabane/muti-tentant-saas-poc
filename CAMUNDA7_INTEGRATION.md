# Camunda 7 Integration Guide

## Overview

This system integrates with Camunda BPM Platform 7, which provides:
- REST API for workflow deployment and execution
- Multi-tenancy support
- Web-based Cockpit for monitoring
- Task management and user tasks
- History and reporting

## Architecture

### Camunda 7 vs Camunda 8

**Camunda 7** (used in this project):
- Traditional REST API architecture
- Embedded or standalone deployment
- Mature ecosystem with extensive features
- Multi-tenancy built-in
- Web applications (Cockpit, Tasklist, Admin)

**Camunda 8** (not used):
- Cloud-native with Zeebe engine
- gRPC-based communication
- Designed for microservices and high scalability
- Different API and deployment model

## Integration Points

### 1. Deployment to Camunda

When deploying to non-prod or production environments:

```javascript
// POST to Camunda REST API
POST http://localhost:8080/engine-rest/deployment/create

FormData:
- deployment-name: unique deployment identifier
- tenant-id: tenant identifier for isolation
- diagram.bpmn: BPMN XML file
```

The system automatically:
- Converts BPMN XML to multipart form data
- Includes tenant ID for isolation
- Stores Camunda deployment ID in metadata

### 2. Process Instance Execution

Starting a workflow instance:

```javascript
// POST to start process by key and tenant
POST http://localhost:8080/engine-rest/process-definition/key/{key}/tenant-id/{tenantId}/start

Body:
{
  "variables": {
    "variableName": {
      "value": "variableValue",
      "type": "String"
    }
  },
  "businessKey": "unique-business-key"
}
```

### 3. Variable Type Mapping

The system automatically converts JSON input to Camunda variables:

| JavaScript Type | Camunda Type |
|----------------|--------------|
| string         | String       |
| number (int)   | Integer      |
| number (float) | Double       |
| boolean        | Boolean      |
| null           | Null         |
| object/array   | Json         |

### 4. Tenant Isolation

Camunda 7 provides native multi-tenancy:
- Each deployment includes a tenant-id
- Process definitions are scoped to tenants
- Queries automatically filter by tenant
- Complete data isolation between tenants

## Workflow Lifecycle

### Test Environment
1. User designs BPMN in visual editor
2. Saves workflow to database
3. Test run simulates execution (no Camunda)
4. Returns mock results

### Non-Production Environment
1. User deploys workflow
2. System calls Camunda REST API
3. BPMN deployed with tenant-id
4. Process definition available in Camunda
5. Can execute real instances
6. Monitor in Camunda Cockpit

### Production Environment
1. Promote from non-prod
2. New deployment to Camunda with tenant-id
3. Selective rollout (0-100%)
4. Real process execution
5. Full monitoring and history

## Camunda Cockpit Access

Access the Camunda web interface:
- URL: http://localhost:8080/camunda
- Default credentials: admin/admin
- Features:
  - View deployed processes
  - Monitor running instances
  - Inspect variables
  - View history
  - Manage incidents

## REST API Examples

### Deploy a Process

```bash
curl -X POST \
  http://localhost:8080/engine-rest/deployment/create \
  -H 'Content-Type: multipart/form-data' \
  -F 'deployment-name=my-deployment' \
  -F 'tenant-id=tenant-123' \
  -F 'diagram.bpmn=@process.bpmn'
```

### Start Process Instance

```bash
curl -X POST \
  http://localhost:8080/engine-rest/process-definition/key/Process_1/tenant-id/tenant-123/start \
  -H 'Content-Type: application/json' \
  -d '{
    "variables": {
      "orderId": {"value": "12345", "type": "String"},
      "amount": {"value": 100.50, "type": "Double"}
    }
  }'
```

### Query Process Instances

```bash
curl -X GET \
  'http://localhost:8080/engine-rest/process-instance?tenantIdIn=tenant-123'
```

### Get Process Instance History

```bash
curl -X GET \
  http://localhost:8080/engine-rest/history/process-instance/{instanceId}
```

## BPMN Best Practices

### Process Definition Key
- Use consistent naming: `Process_OrderFulfillment`
- Key is used to start instances
- Must be unique per tenant

### Service Tasks
- Implement external task pattern
- Use REST connectors for API calls
- Configure retry logic

### User Tasks
- Assign to groups or users
- Use forms for data input
- Configure task listeners

### Error Handling
- Use BPMN error events
- Implement compensation
- Configure incident handling

## DMN Integration

Camunda 7 also supports DMN (Decision Model and Notation):

```javascript
// Deploy DMN alongside BPMN
FormData:
- decision.dmn: DMN XML file
- tenant-id: tenant identifier

// Evaluate decision
POST /engine-rest/decision-definition/key/{key}/tenant-id/{tenantId}/evaluate
```

## Monitoring and Operations

### Health Check
```bash
curl http://localhost:8080/engine-rest/engine
```

### Metrics
```bash
curl http://localhost:8080/engine-rest/metrics
```

### Job Execution
- Camunda handles async jobs automatically
- Configure job executor in application.yaml
- Monitor failed jobs in Cockpit

## Production Considerations

### Performance
- Configure connection pool size
- Tune job executor threads
- Enable history cleanup
- Use database indexes

### Security
- Change default admin password
- Enable authentication for REST API
- Use HTTPS in production
- Implement authorization rules

### High Availability
- Deploy multiple Camunda instances
- Share database between instances
- Use load balancer
- Configure cluster settings

### Backup
- Regular database backups
- Export process definitions
- Archive history data
- Document deployment procedures

## Troubleshooting

### Deployment Fails
- Check BPMN XML validity
- Verify tenant-id format
- Review Camunda logs
- Test in Camunda Modeler first

### Process Won't Start
- Verify process definition key
- Check tenant-id matches
- Ensure process is deployed
- Review variable types

### Instance Stuck
- Check for incidents in Cockpit
- Review job execution logs
- Verify external task workers
- Check service task configurations

## Resources

- [Camunda 7 Documentation](https://docs.camunda.org/manual/7.20/)
- [REST API Reference](https://docs.camunda.org/manual/7.20/reference/rest/)
- [BPMN 2.0 Tutorial](https://camunda.com/bpmn/)
- [DMN Tutorial](https://camunda.com/dmn/)
