# Requirements Document

## Introduction

This feature enhances the Apps section of the multi-tenant workflow SaaS platform by adding comprehensive configuration management capabilities. Users will be able to edit app configurations through a dedicated app details page, including source selection, enrichment API configurations, parameter and model uploads, and publisher settings. The configuration supports multiple file formats (JSON, CSV, Avro schema, properties files) and enables complex mapping definitions for data transformation.

## Glossary

- **App**: A logical container for BPMN and DMN resources within a tenant's workspace
- **Configuration_Manager**: The system component responsible for storing and managing app configurations
- **File_Upload_Handler**: The system component that processes and validates uploaded configuration files
- **Enrichment_API**: An external API integration that enriches workflow data during execution
- **Publisher**: The system component that publishes workflow execution results to external systems
- **OpenAPI_Document**: A JSON file describing REST API structure following OpenAPI specification
- **Avro_Schema**: A JSON file defining data structure using Apache Avro schema format
- **Request_Mapping**: A key-value pair mapping source data fields to target field names
- **EEH**: Event Hub integration for publishing workflow results
- **Warehouse_Publisher**: A specific publisher type that sends data to a data warehouse

## Requirements

### Requirement 1: Navigate to App Configuration

**User Story:** As a user, I want to access app configuration from the Apps page, so that I can edit my app settings.

#### Acceptance Criteria

1. THE Apps_Page SHALL display an edit button for each app in the tree view
2. WHEN a user clicks the edit button, THE System SHALL navigate to the app details page
3. THE App_Details_Page SHALL display the app name and current configuration
4. THE System SHALL load existing configuration data for the selected app

### Requirement 2: Configure App Source

**User Story:** As a user, I want to specify the data source for my app, so that the system knows where to retrieve input data.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display a source selection dropdown
2. THE Source_Dropdown SHALL contain two options: "eeh" and "api"
3. WHEN a user selects a source option, THE Configuration_Manager SHALL store the selection
4. THE System SHALL validate that exactly one source is selected before saving

### Requirement 3: Configure Enrichment API

**User Story:** As a user, I want to configure multiple enrichment APIs, so that I can enhance workflow data with external services.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display an enrichment API configuration section
2. THE System SHALL allow users to add multiple enrichment API entries
3. FOR EACH enrichment API entry, THE File_Upload_Handler SHALL accept an OpenAPI document in JSON format
4. THE File_Upload_Handler SHALL validate that uploaded OpenAPI documents conform to OpenAPI specification
5. FOR EACH enrichment API entry, THE System SHALL provide two text input fields for request mapping
6. WHEN a user enters request mappings, THE Configuration_Manager SHALL store them in request-mappings.properties format
7. FOR EACH enrichment API entry, THE System SHALL provide key-value pair inputs for configuration properties
8. THE Configuration_Manager SHALL store configuration properties in config.properties format
9. THE System SHALL allow users to remove enrichment API entries

### Requirement 4: Upload Parameters Configuration

**User Story:** As a user, I want to upload a parameters CSV file, so that I can define workflow parameters.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display a parameters upload section
2. THE File_Upload_Handler SHALL accept CSV file uploads for parameters
3. THE File_Upload_Handler SHALL validate that uploaded files are valid CSV format
4. WHEN a parameters file is uploaded, THE Configuration_Manager SHALL store the file content
5. THE System SHALL display the filename of the currently uploaded parameters file
6. THE System SHALL allow users to replace the parameters file

### Requirement 5: Upload Model Configuration

**User Story:** As a user, I want to upload a model CSV file, so that I can define data models for my workflows.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display a model upload section
2. THE File_Upload_Handler SHALL accept CSV file uploads for models
3. THE File_Upload_Handler SHALL validate that uploaded files are valid CSV format
4. WHEN a model file is uploaded, THE Configuration_Manager SHALL store the file content
5. THE System SHALL display the filename of the currently uploaded model file
6. THE System SHALL allow users to replace the model file

### Requirement 6: Configure API Publisher

**User Story:** As a user, I want to configure API-based publishing, so that workflow results can be sent to external APIs.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display a publisher configuration section
2. THE Publisher_Section SHALL provide an API publisher option
3. WHEN API publisher is selected, THE File_Upload_Handler SHALL accept an OpenAPI document in JSON format
4. THE File_Upload_Handler SHALL validate that uploaded OpenAPI documents conform to OpenAPI specification
5. WHEN API publisher is selected, THE System SHALL provide two text input fields for request mapping
6. THE Configuration_Manager SHALL store API publisher request mappings in request-mappings.properties format
7. WHEN API publisher is selected, THE System SHALL provide key-value pair inputs for configuration properties
8. THE Configuration_Manager SHALL store API publisher configuration in config.properties format

### Requirement 7: Configure EEH Publisher

**User Story:** As a user, I want to configure Event Hub publishing, so that workflow results can be sent to event streaming systems.

#### Acceptance Criteria

1. THE Publisher_Section SHALL provide an EEH publisher option
2. WHEN EEH publisher is selected, THE System SHALL display a warehouse publisher configuration subsection
3. THE File_Upload_Handler SHALL accept Avro schema file uploads for warehouse publisher
4. THE File_Upload_Handler SHALL validate that uploaded files are valid Avro schema format
5. THE System SHALL provide text input fields for data field mappings in the format "source -> target"
6. THE Configuration_Manager SHALL store warehouse publisher mappings in mappings.properties format
7. THE System SHALL validate that Avro schema files are valid JSON documents

### Requirement 8: Persist Configuration Changes

**User Story:** As a user, I want to save my configuration changes, so that they are applied to my app.

#### Acceptance Criteria

1. THE App_Details_Page SHALL display a save button
2. WHEN a user clicks save, THE Configuration_Manager SHALL validate all configuration data
3. IF validation fails, THEN THE System SHALL display specific error messages for each invalid field
4. WHEN validation succeeds, THE Configuration_Manager SHALL persist all configuration changes to the database
5. WHEN configuration is saved successfully, THE System SHALL display a success message
6. WHEN configuration is saved successfully, THE System SHALL update the app's updated_at timestamp

### Requirement 9: Handle File Upload Errors

**User Story:** As a user, I want to receive clear error messages when file uploads fail, so that I can correct the issues.

#### Acceptance Criteria

1. IF an uploaded file exceeds the maximum size limit, THEN THE File_Upload_Handler SHALL return an error message with the size limit
2. IF an uploaded file has an invalid format, THEN THE File_Upload_Handler SHALL return an error message specifying the expected format
3. IF an uploaded JSON file is malformed, THEN THE File_Upload_Handler SHALL return an error message with the parsing error location
4. IF an uploaded CSV file has invalid structure, THEN THE File_Upload_Handler SHALL return an error message with the row and column information
5. THE System SHALL display file upload errors inline near the upload component

### Requirement 10: Validate Request Mappings

**User Story:** As a user, I want the system to validate my request mappings, so that I can ensure they are correctly formatted.

#### Acceptance Criteria

1. THE System SHALL validate that request mapping entries follow the format "source -> target"
2. IF a request mapping entry is malformed, THEN THE System SHALL display an error message indicating the correct format
3. THE System SHALL validate that source and target field names contain only alphanumeric characters, dots, and underscores
4. THE System SHALL allow multiple request mapping entries separated by pipe characters
5. THE System SHALL trim whitespace from source and target field names before storing

### Requirement 11: Display Current Configuration

**User Story:** As a user, I want to see my current app configuration when I open the app details page, so that I can review and modify existing settings.

#### Acceptance Criteria

1. WHEN the app details page loads, THE Configuration_Manager SHALL retrieve the current configuration for the app
2. THE System SHALL populate the source dropdown with the currently selected source
3. THE System SHALL display all existing enrichment API entries with their configurations
4. THE System SHALL display the filenames of uploaded parameters and model files
5. THE System SHALL display the current publisher configuration
6. IF no configuration exists, THEN THE System SHALL display empty form fields with default values

### Requirement 12: Support Multi-Tenant Isolation

**User Story:** As a tenant, I want my app configurations to be isolated from other tenants, so that my data remains secure.

#### Acceptance Criteria

1. THE Configuration_Manager SHALL associate all configuration data with the tenant ID from the JWT token
2. THE System SHALL prevent users from accessing app configurations belonging to other tenants
3. WHEN retrieving configuration data, THE Configuration_Manager SHALL filter by tenant ID
4. THE System SHALL validate that the app being configured belongs to the authenticated tenant

