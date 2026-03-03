# Requirements Document

## Introduction

This feature enables BPMN workflows to reference and execute DMN decision tables through Business Rule Tasks. It provides dependency management to ensure DMN workflows are deployed alongside BPMN workflows and prevents deletion of DMN workflows that are actively referenced.

## Glossary

- **BPMN_Workflow**: A Business Process Model and Notation workflow stored as XML
- **DMN_Workflow**: A Decision Model and Notation decision table stored as XML
- **Business_Rule_Task**: A BPMN task element that executes a DMN decision
- **Decision_Reference**: The decision key or ID stored in BPMN XML that links to a DMN workflow
- **DMN_Dependency**: A DMN workflow required by a BPMN workflow for execution
- **Workflow_Designer**: The frontend component using bpmn-js for visual BPMN editing
- **Properties_Panel**: The bpmn-js component for configuring BPMN element properties
- **Deployment_Service**: The backend service that deploys workflows to Camunda
- **Camunda_Engine**: The Camunda BPM Platform 7 workflow execution engine

## Requirements

### Requirement 1: Configure Business Rule Task DMN References

**User Story:** As a workflow designer, I want to configure Business Rule Tasks to reference DMN workflows, so that my BPMN can execute decision logic.

#### Acceptance Criteria

1. WHEN a user selects a Business Rule Task in the Workflow_Designer, THE Properties_Panel SHALL display a DMN reference configuration field
2. THE Properties_Panel SHALL list all available DMN_Workflows for the current tenant
3. WHEN a user selects a DMN_Workflow from the list, THE Workflow_Designer SHALL store the decision reference in the BPMN XML decisionRef attribute
4. THE Workflow_Designer SHALL validate that the selected DMN_Workflow exists before saving
5. WHEN a user clears the DMN reference, THE Workflow_Designer SHALL remove the decisionRef attribute from the BPMN XML

### Requirement 2: Extract DMN Dependencies from BPMN

**User Story:** As a system, I want to extract DMN dependencies from BPMN XML, so that I can deploy all required DMN workflows together.

#### Acceptance Criteria

1. WHEN a BPMN_Workflow is saved, THE Deployment_Service SHALL parse the BPMN XML for Business Rule Tasks
2. THE Deployment_Service SHALL extract all decisionRef attributes from Business Rule Tasks
3. THE Deployment_Service SHALL resolve each Decision_Reference to a DMN_Workflow ID
4. THE Deployment_Service SHALL return a list of DMN_Dependency IDs for the BPMN_Workflow
5. IF a Decision_Reference cannot be resolved to a DMN_Workflow, THEN THE Deployment_Service SHALL return an error with the unresolved reference

### Requirement 3: Deploy BPMN with DMN Dependencies

**User Story:** As a deployment manager, I want BPMN workflows to deploy with all DMN dependencies, so that Business Rule Tasks execute successfully.

#### Acceptance Criteria

1. WHEN a BPMN_Workflow is deployed to Camunda_Engine, THE Deployment_Service SHALL extract all DMN_Dependency IDs
2. THE Deployment_Service SHALL retrieve the DMN XML for each DMN_Dependency
3. THE Deployment_Service SHALL create a single Camunda deployment containing the BPMN XML and all DMN XML files
4. THE Deployment_Service SHALL validate that all DMN_Dependencies exist before deployment
5. IF any DMN_Dependency is missing, THEN THE Deployment_Service SHALL return an error listing the missing DMN workflows
6. THE Deployment_Service SHALL deploy to Camunda_Engine only after all validations pass

### Requirement 4: Prevent Deletion of Referenced DMN Workflows

**User Story:** As a system administrator, I want to prevent deletion of DMN workflows that are referenced by BPMN, so that deployed workflows continue to function.

#### Acceptance Criteria

1. WHEN a user attempts to delete a DMN_Workflow, THE Deployment_Service SHALL check for BPMN workflows that reference it
2. IF any BPMN_Workflow contains a Decision_Reference to the DMN_Workflow, THEN THE Deployment_Service SHALL reject the deletion request
3. THE Deployment_Service SHALL return an error message listing all BPMN workflows that reference the DMN_Workflow
4. THE Deployment_Service SHALL allow deletion only when no BPMN workflows reference the DMN_Workflow
5. THE Deployment_Service SHALL check references across all deployment environments (test, non-prod, production)

### Requirement 5: Execute Business Rule Tasks

**User Story:** As a workflow executor, I want Business Rule Tasks to invoke DMN decisions during execution, so that decision logic is evaluated.

#### Acceptance Criteria

1. WHEN a BPMN process instance reaches a Business Rule Task, THE Camunda_Engine SHALL resolve the Decision_Reference
2. THE Camunda_Engine SHALL execute the referenced DMN_Workflow with the current process variables
3. THE Camunda_Engine SHALL store the DMN decision result in the process variables
4. IF the Decision_Reference cannot be resolved, THEN THE Camunda_Engine SHALL throw an error and halt the process instance
5. THE Camunda_Engine SHALL log the DMN execution result for audit purposes

### Requirement 6: List DMN Workflows for Configuration

**User Story:** As a workflow designer, I want to see all available DMN workflows when configuring a Business Rule Task, so that I can select the correct decision table.

#### Acceptance Criteria

1. WHEN the Properties_Panel requests DMN workflows, THE Deployment_Service SHALL return all DMN_Workflows for the current tenant
2. THE Deployment_Service SHALL include the workflow name, decision key, and version for each DMN_Workflow
3. THE Deployment_Service SHALL filter results to only DMN type workflows
4. THE Deployment_Service SHALL order results alphabetically by workflow name
5. THE Deployment_Service SHALL return an empty list if no DMN workflows exist

### Requirement 7: Validate DMN References Before Deployment

**User Story:** As a deployment manager, I want DMN references validated before deployment, so that I catch configuration errors early.

#### Acceptance Criteria

1. WHEN a BPMN_Workflow is submitted for deployment, THE Deployment_Service SHALL validate all Decision_References
2. THE Deployment_Service SHALL verify that each referenced DMN_Workflow exists in the database
3. THE Deployment_Service SHALL verify that each referenced DMN_Workflow belongs to the same tenant
4. IF any validation fails, THEN THE Deployment_Service SHALL return a detailed error message
5. THE Deployment_Service SHALL prevent deployment to Camunda_Engine until all validations pass

### Requirement 8: Parse BPMN XML for Decision References

**User Story:** As a system, I want to parse BPMN XML for decision references, so that I can identify DMN dependencies.

#### Acceptance Criteria

1. THE Deployment_Service SHALL parse BPMN XML using a standards-compliant XML parser
2. THE Deployment_Service SHALL locate all elements with type "bpmn:BusinessRuleTask"
3. THE Deployment_Service SHALL extract the "camunda:decisionRef" attribute from each Business Rule Task
4. THE Deployment_Service SHALL handle BPMN XML with namespaces correctly
5. IF the BPMN XML is malformed, THEN THE Deployment_Service SHALL return a parsing error

