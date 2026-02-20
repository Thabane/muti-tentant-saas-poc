# Requirements Document

## Introduction

This document specifies the requirements for enhancing the existing "Workflows" section by renaming it to "Apps" and introducing a hierarchical structure that groups BPMN and DMN resources. The system will automatically generate API paths for services and assign unique API keys to each app. This enhancement is part of a multi-tenant workflow SaaS application with a Java backend (Spring Boot) and React frontend, integrated with Camunda for workflow orchestration.

## Glossary

- **App**: A logical container that groups related BPMN and DMN resources
- **BPMN**: Business Process Model and Notation resource representing the orchestration layer for a service endpoint
- **DMN**: Decision Model and Notation resource representing decision logic associated with a BPMN
- **Service**: A BPMN resource that serves as the orchestration layer for a service endpoint
- **API_Path**: The automatically generated endpoint path for accessing a service
- **API_Key**: A unique authentication key assigned to each app
- **Tenant**: An isolated customer instance in the multi-tenant system
- **Sub_Service**: A logical grouping within a tenant's service hierarchy
- **UI**: The React frontend user interface
- **Backend**: The Java Spring Boot backend system

## Requirements

### Requirement 1: Section Renaming

**User Story:** As a user, I want the "Workflows" section renamed to "Apps", so that the terminology better reflects the logical grouping of resources.

#### Acceptance Criteria

1. THE UI SHALL display "Apps" instead of "Workflows" in the navigation menu
2. THE UI SHALL display "Apps" instead of "Workflows" in the section header
3. THE UI SHALL display "Apps" instead of "Workflows" in all breadcrumbs and page titles
4. THE Backend SHALL use "Apps" terminology in API responses and documentation

### Requirement 2: Resource Grouping

**User Story:** As a user, I want BPMN and DMN resources grouped under each app, so that I can easily understand which resources belong together.

#### Acceptance Criteria

1. WHEN displaying the Apps section, THE UI SHALL group all BPMN and DMN resources under their respective app containers
2. WHEN a user creates a new BPMN or DMN resource, THE Backend SHALL associate it with a specific app
3. WHEN querying resources, THE Backend SHALL return resources organized by app hierarchy
4. THE Backend SHALL maintain the relationship between apps and their associated BPMN and DMN resources in the database

### Requirement 3: BPMN-DMN Hierarchy

**User Story:** As a user, I want to see DMN resources as children of their parent BPMN resources, so that I can understand the decision logic associated with each service.

#### Acceptance Criteria

1. WHEN displaying a BPMN resource, THE UI SHALL show all associated DMN resources as child nodes in a tree structure
2. WHEN a user expands a BPMN node, THE UI SHALL display all linked DMN resources beneath it
3. THE Backend SHALL maintain the parent-child relationship between BPMN and DMN resources
4. WHEN a BPMN has multiple DMNs, THE UI SHALL display all of them as children of that BPMN
5. WHEN a BPMN has no associated DMNs, THE UI SHALL display the BPMN without child nodes

### Requirement 4: Service Representation

**User Story:** As a user, I want each BPMN to be clearly identified as a service, so that I understand its role as an orchestration layer.

#### Acceptance Criteria

1. THE UI SHALL label each BPMN row as "Service" in the display
2. THE UI SHALL use service-related terminology when referring to BPMN resources
3. THE Backend SHALL treat BPMN resources as service definitions in the orchestration layer
4. WHEN displaying service details, THE UI SHALL show that the BPMN represents the orchestration layer for the service endpoint

### Requirement 5: API Path Generation

**User Story:** As a developer, I want the system to automatically generate API paths for each service, so that I can access service endpoints without manual configuration.

#### Acceptance Criteria

1. WHEN a BPMN service is created, THE Backend SHALL automatically generate an API path following the pattern /{tenant-id}/{sub-service}/{service-name}/v1
2. THE Backend SHALL convert the service name to lowercase and replace spaces with hyphens in the API path
3. WHEN displaying a service, THE UI SHALL show the generated API path
4. THE Backend SHALL ensure the generated API path is unique within the tenant and sub-service scope
5. WHEN the service name is "Income Service", THE Backend SHALL generate the API path /tenant-id/sub-service/income-service/v1

### Requirement 6: API Key Management

**User Story:** As a developer, I want each app to have a unique API key, so that I can securely authenticate API requests to the app's services.

#### Acceptance Criteria

1. WHEN an app is created, THE Backend SHALL generate a unique API key for that app
2. THE Backend SHALL store the API key securely in the database
3. WHEN displaying app details, THE UI SHALL show the API key with options to copy or regenerate it
4. THE Backend SHALL validate API keys when processing requests to app services
5. WHEN a user requests to regenerate an API key, THE Backend SHALL create a new unique key and invalidate the old one
6. THE Backend SHALL ensure API keys are unique across all apps in the system

### Requirement 7: Data Model Updates

**User Story:** As a system architect, I want the data model to support the new app hierarchy, so that the system can persist and query the relationships between apps, BPMNs, and DMNs.

#### Acceptance Criteria

1. THE Backend SHALL define an App entity with fields for id, name, apiKey, tenantId, and timestamps
2. THE Backend SHALL define a relationship where one App can have many BPMN resources
3. THE Backend SHALL define a relationship where one BPMN can have many DMN resources
4. THE Backend SHALL ensure referential integrity between App, BPMN, and DMN entities
5. WHEN an app is deleted, THE Backend SHALL handle cascading deletes or prevent deletion if resources exist

### Requirement 8: Migration Support

**User Story:** As a system administrator, I want existing workflow data to be migrated to the new app structure, so that no data is lost during the transition.

#### Acceptance Criteria

1. THE Backend SHALL provide a migration mechanism to convert existing workflow data to the new app structure
2. WHEN migration runs, THE Backend SHALL create default apps for existing BPMN resources that don't have an app association
3. WHEN migration runs, THE Backend SHALL preserve all existing BPMN-DMN relationships
4. THE Backend SHALL generate API keys for all apps created during migration
5. THE Backend SHALL generate API paths for all existing BPMN services during migration
