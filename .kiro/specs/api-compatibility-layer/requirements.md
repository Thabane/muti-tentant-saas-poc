# Requirements Document

## Introduction

This specification defines the REST API compatibility layer that ensures the React frontend can operate seamlessly with both the legacy Node.js backend and the new Java Spring Boot backend during the migration period. The compatibility layer guarantees exact endpoint path matching, request/response format compatibility, HTTP status code alignment, and error response format consistency.

## Glossary

- **API_Compatibility_Layer**: The Spring Boot REST controller layer that maintains exact compatibility with Node.js Express endpoints
- **Request_DTO**: Data Transfer Object for incoming HTTP requests with validation rules
- **Response_DTO**: Data Transfer Object for outgoing HTTP responses with JSON serialization
- **Error_Response**: Standardized error response format matching Node.js error structure
- **Frontend**: React application that consumes the REST API
- **Node_Backend**: Legacy Express.js backend being replaced
- **Java_Backend**: New Spring Boot backend with compatibility requirements
- **HTTP_Status_Code**: Standard HTTP response status codes (200, 201, 400, 401, 404, 500)
- **Content_Negotiation**: HTTP header-based content type handling (Content-Type, Accept)
- **Path_Variable**: Dynamic URL segment (e.g., :id in /api/workflows/:id)
- **Query_Parameter**: URL query string parameter (e.g., ?page=1&size=10)
- **Jackson**: JSON serialization/deserialization library for Spring Boot
- **Jakarta_Validation**: Bean validation framework for request validation

## Requirements

### Requirement 1: Endpoint Path Compatibility

**User Story:** As a frontend developer, I want all API endpoint paths to remain unchanged, so that the frontend code requires no modifications during backend migration.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL expose POST /api/tenants/register for tenant registration
2. THE API_Compatibility_Layer SHALL expose POST /api/tenants/login for tenant authentication
3. THE API_Compatibility_Layer SHALL expose GET /api/tenants/me for retrieving current tenant information
4. THE API_Compatibility_Layer SHALL expose PATCH /api/tenants/onboarding for updating onboarding status
5. THE API_Compatibility_Layer SHALL expose POST /api/workflows for creating workflows
6. THE API_Compatibility_Layer SHALL expose GET /api/workflows for listing workflows
7. THE API_Compatibility_Layer SHALL expose GET /api/workflows/:id for retrieving a specific workflow
8. THE API_Compatibility_Layer SHALL expose PUT /api/workflows/:id for updating workflows
9. THE API_Compatibility_Layer SHALL expose POST /api/workflows/:id/test for testing workflows
10. THE API_Compatibility_Layer SHALL expose POST /api/deployments for creating deployments
11. THE API_Compatibility_Layer SHALL expose GET /api/deployments for listing deployments
12. THE API_Compatibility_Layer SHALL expose POST /api/deployments/:id/promote for promoting deployments
13. THE API_Compatibility_Layer SHALL expose POST /api/deployments/:id/rollout for adjusting rollout percentages
14. THE API_Compatibility_Layer SHALL expose POST /api/deployments/:id/execute for executing workflows
15. THE API_Compatibility_Layer SHALL expose GET /health for health check monitoring

### Requirement 2: Request DTO Validation Compatibility

**User Story:** As a backend developer, I want request validation to match Node.js behavior exactly, so that the frontend receives consistent validation error messages.

#### Acceptance Criteria

1. WHEN a request contains invalid data, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request
2. WHEN a required field is missing, THE API_Compatibility_Layer SHALL return an error message identifying the missing field
3. WHEN a field fails validation constraints, THE API_Compatibility_Layer SHALL return an error message describing the constraint violation
4. THE Request_DTO SHALL validate email format for tenant registration and login
5. THE Request_DTO SHALL validate password minimum length of 8 characters
6. THE Request_DTO SHALL validate required fields using Jakarta_Validation @NotNull and @NotBlank annotations
7. THE Request_DTO SHALL validate string length constraints using @Size annotation
8. WHEN multiple validation errors occur, THE API_Compatibility_Layer SHALL return all validation errors in a single response
9. THE API_Compatibility_Layer SHALL parse JSON request bodies using Jackson with the same field naming convention as Node_Backend
10. WHEN request body exceeds size limit, THE API_Compatibility_Layer SHALL return HTTP 413 Payload Too Large

### Requirement 3: Response DTO Format Compatibility

**User Story:** As a frontend developer, I want response JSON structure to remain identical to Node.js responses, so that existing frontend parsing logic continues to work without changes.

#### Acceptance Criteria

1. THE Response_DTO SHALL serialize to JSON using camelCase field naming matching Node_Backend responses
2. WHEN creating a resource, THE API_Compatibility_Layer SHALL return HTTP 201 Created with the created resource in the response body
3. WHEN retrieving a resource, THE API_Compatibility_Layer SHALL return HTTP 200 OK with the resource in the response body
4. WHEN updating a resource, THE API_Compatibility_Layer SHALL return HTTP 200 OK with the updated resource in the response body
5. WHEN listing resources, THE API_Compatibility_Layer SHALL return HTTP 200 OK with an array of resources
6. THE Response_DTO SHALL include all fields present in Node_Backend responses
7. THE Response_DTO SHALL exclude null fields from JSON output using Jackson @JsonInclude(JsonInclude.Include.NON_NULL)
8. THE Response_DTO SHALL format date/time fields as ISO 8601 strings matching Node_Backend format
9. THE Response_DTO SHALL serialize nested objects with the same structure as Node_Backend responses
10. WHEN a tenant registers, THE Response_DTO SHALL include id, email, companyName, and token fields

### Requirement 4: HTTP Status Code Compatibility

**User Story:** As a frontend developer, I want HTTP status codes to match Node.js backend behavior, so that existing error handling logic continues to function correctly.

#### Acceptance Criteria

1. WHEN a resource is created successfully, THE API_Compatibility_Layer SHALL return HTTP 201 Created
2. WHEN a request succeeds, THE API_Compatibility_Layer SHALL return HTTP 200 OK
3. WHEN a resource is not found, THE API_Compatibility_Layer SHALL return HTTP 404 Not Found
4. WHEN authentication fails, THE API_Compatibility_Layer SHALL return HTTP 401 Unauthorized
5. WHEN authorization fails, THE API_Compatibility_Layer SHALL return HTTP 403 Forbidden
6. WHEN request validation fails, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request
7. WHEN a server error occurs, THE API_Compatibility_Layer SHALL return HTTP 500 Internal Server Error
8. WHEN a resource conflict occurs, THE API_Compatibility_Layer SHALL return HTTP 409 Conflict
9. WHEN request payload is too large, THE API_Compatibility_Layer SHALL return HTTP 413 Payload Too Large
10. WHEN health check succeeds, THE API_Compatibility_Layer SHALL return HTTP 200 OK

### Requirement 5: Error Response Format Compatibility

**User Story:** As a frontend developer, I want error responses to have the same JSON structure as Node.js errors, so that existing error display components work without modification.

#### Acceptance Criteria

1. THE Error_Response SHALL include an "error" field containing the error message
2. THE Error_Response SHALL include a "message" field with a human-readable description
3. WHEN validation fails, THE Error_Response SHALL include a "details" array listing all validation errors
4. THE Error_Response SHALL use the same field names as Node_Backend error responses
5. WHEN a resource is not found, THE Error_Response SHALL include message "Resource not found"
6. WHEN authentication fails, THE Error_Response SHALL include message "Invalid credentials"
7. WHEN authorization fails, THE Error_Response SHALL include message "Access denied"
8. THE Error_Response SHALL be generated by a global exception handler using @RestControllerAdvice
9. THE Error_Response SHALL serialize to JSON using Jackson with camelCase field naming
10. WHEN an unexpected error occurs, THE Error_Response SHALL not expose internal implementation details

### Requirement 6: Content Negotiation Compatibility

**User Story:** As a frontend developer, I want Content-Type and Accept header handling to work identically to Node.js, so that HTTP client configuration remains unchanged.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL accept requests with Content-Type: application/json
2. THE API_Compatibility_Layer SHALL return responses with Content-Type: application/json
3. WHEN Accept header is not specified, THE API_Compatibility_Layer SHALL default to application/json
4. WHEN Content-Type is missing for POST/PUT/PATCH requests, THE API_Compatibility_Layer SHALL return HTTP 415 Unsupported Media Type
5. THE API_Compatibility_Layer SHALL parse JSON request bodies using UTF-8 encoding
6. THE API_Compatibility_Layer SHALL serialize JSON responses using UTF-8 encoding
7. THE API_Compatibility_Layer SHALL set charset=utf-8 in Content-Type response header
8. WHEN Accept header specifies unsupported media type, THE API_Compatibility_Layer SHALL return HTTP 406 Not Acceptable
9. THE API_Compatibility_Layer SHALL handle CORS preflight requests matching Node_Backend CORS configuration
10. THE API_Compatibility_Layer SHALL include appropriate CORS headers in responses

### Requirement 7: Request Body Size Limit Compatibility

**User Story:** As a backend developer, I want request body size limits to match Node.js configuration, so that large BPMN workflow definitions are handled consistently.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL accept request bodies up to 10MB in size
2. WHEN request body exceeds 10MB, THE API_Compatibility_Layer SHALL return HTTP 413 Payload Too Large
3. THE API_Compatibility_Layer SHALL configure Spring Boot max request size using spring.servlet.multipart.max-request-size property
4. THE API_Compatibility_Layer SHALL configure Spring Boot max file size using spring.servlet.multipart.max-file-size property
5. THE Error_Response for oversized requests SHALL include message "Request payload too large"
6. THE API_Compatibility_Layer SHALL handle large BPMN XML workflow definitions without truncation
7. THE API_Compatibility_Layer SHALL handle large DMN XML decision table definitions without truncation
8. WHEN streaming large responses, THE API_Compatibility_Layer SHALL use chunked transfer encoding
9. THE API_Compatibility_Layer SHALL configure Tomcat max-http-header-size to match Node_Backend
10. THE API_Compatibility_Layer SHALL log warnings when requests approach size limits

### Requirement 8: Query Parameter Parsing Compatibility

**User Story:** As a frontend developer, I want query parameter parsing to work identically to Node.js, so that pagination and filtering logic remains unchanged.

#### Acceptance Criteria

1. WHEN GET /api/workflows includes ?page=1&size=10, THE API_Compatibility_Layer SHALL parse page and size as integers
2. WHEN query parameter is missing, THE API_Compatibility_Layer SHALL use the same default values as Node_Backend
3. WHEN query parameter has invalid format, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request
4. THE API_Compatibility_Layer SHALL parse boolean query parameters as true/false strings
5. THE API_Compatibility_Layer SHALL parse array query parameters using comma separation
6. THE API_Compatibility_Layer SHALL decode URL-encoded query parameter values
7. THE API_Compatibility_Layer SHALL handle query parameters with special characters
8. WHEN multiple values are provided for same parameter, THE API_Compatibility_Layer SHALL use the same precedence rules as Node_Backend
9. THE API_Compatibility_Layer SHALL trim whitespace from query parameter values
10. THE API_Compatibility_Layer SHALL validate query parameter constraints using Jakarta_Validation

### Requirement 9: Path Variable Parsing Compatibility

**User Story:** As a frontend developer, I want path variable parsing to work identically to Node.js, so that resource identification logic remains unchanged.

#### Acceptance Criteria

1. WHEN GET /api/workflows/:id is called, THE API_Compatibility_Layer SHALL extract id as a Long integer
2. WHEN path variable has invalid format, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request
3. THE API_Compatibility_Layer SHALL validate path variable constraints using Jakarta_Validation
4. WHEN path variable represents a non-existent resource, THE API_Compatibility_Layer SHALL return HTTP 404 Not Found
5. THE API_Compatibility_Layer SHALL decode URL-encoded path variable values
6. THE API_Compatibility_Layer SHALL handle path variables with special characters
7. THE API_Compatibility_Layer SHALL use @PathVariable annotation for path variable extraction
8. WHEN path variable is null or empty, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request
9. THE API_Compatibility_Layer SHALL validate UUID format for path variables representing UUIDs
10. THE Error_Response for invalid path variables SHALL include the parameter name and expected format

### Requirement 10: Authentication Token Compatibility

**User Story:** As a frontend developer, I want JWT token handling to work identically to Node.js, so that authentication logic remains unchanged.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL accept JWT tokens in Authorization header with "Bearer " prefix
2. WHEN Authorization header is missing for protected endpoints, THE API_Compatibility_Layer SHALL return HTTP 401 Unauthorized
3. WHEN JWT token is invalid, THE API_Compatibility_Layer SHALL return HTTP 401 Unauthorized
4. WHEN JWT token is expired, THE API_Compatibility_Layer SHALL return HTTP 401 Unauthorized
5. THE API_Compatibility_Layer SHALL extract tenant ID from JWT token claims
6. THE API_Compatibility_Layer SHALL validate JWT signature using the same secret key as Node_Backend
7. THE API_Compatibility_Layer SHALL use the same JWT claims structure as Node_Backend
8. WHEN login succeeds, THE Response_DTO SHALL include a "token" field containing the JWT
9. THE API_Compatibility_Layer SHALL set JWT expiration time matching Node_Backend configuration
10. THE API_Compatibility_Layer SHALL include tenant ID and email in JWT claims

### Requirement 11: Health Check Endpoint Compatibility

**User Story:** As a DevOps engineer, I want the health check endpoint to return the same format as Node.js, so that monitoring tools continue to work without reconfiguration.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL expose GET /health endpoint without authentication
2. WHEN application is healthy, THE API_Compatibility_Layer SHALL return HTTP 200 OK
3. THE Response_DTO for health check SHALL include "status" field with value "ok"
4. THE Response_DTO for health check SHALL include "timestamp" field with ISO 8601 formatted date
5. WHEN database connection fails, THE API_Compatibility_Layer SHALL return HTTP 503 Service Unavailable
6. THE Error_Response for unhealthy status SHALL include details about failed components
7. THE API_Compatibility_Layer SHALL check database connectivity as part of health check
8. THE API_Compatibility_Layer SHALL check Camunda engine status as part of health check
9. THE API_Compatibility_Layer SHALL respond to health checks within 1 second
10. THE API_Compatibility_Layer SHALL log health check failures for monitoring

### Requirement 12: CORS Configuration Compatibility

**User Story:** As a frontend developer, I want CORS configuration to match Node.js settings, so that cross-origin requests continue to work without changes.

#### Acceptance Criteria

1. THE API_Compatibility_Layer SHALL allow cross-origin requests from configured frontend origins
2. THE API_Compatibility_Layer SHALL include Access-Control-Allow-Origin header in responses
3. THE API_Compatibility_Layer SHALL include Access-Control-Allow-Methods header listing allowed HTTP methods
4. THE API_Compatibility_Layer SHALL include Access-Control-Allow-Headers header listing allowed request headers
5. THE API_Compatibility_Layer SHALL include Access-Control-Allow-Credentials: true for authenticated requests
6. THE API_Compatibility_Layer SHALL handle OPTIONS preflight requests
7. THE API_Compatibility_Layer SHALL set Access-Control-Max-Age for preflight cache duration
8. THE API_Compatibility_Layer SHALL use the same allowed origins list as Node_Backend
9. THE API_Compatibility_Layer SHALL use the same allowed headers list as Node_Backend
10. THE API_Compatibility_Layer SHALL configure CORS using Spring Boot CorsConfiguration

### Requirement 13: Tenant Isolation Compatibility

**User Story:** As a backend developer, I want tenant isolation to work identically to Node.js, so that multi-tenant data security is maintained during migration.

#### Acceptance Criteria

1. WHEN a tenant accesses resources, THE API_Compatibility_Layer SHALL filter results by tenant ID from JWT token
2. WHEN a tenant attempts to access another tenant's resource, THE API_Compatibility_Layer SHALL return HTTP 404 Not Found
3. THE API_Compatibility_Layer SHALL extract tenant ID from JWT token for all authenticated requests
4. THE API_Compatibility_Layer SHALL pass tenant ID to service layer for data filtering
5. THE API_Compatibility_Layer SHALL validate tenant ownership before allowing resource modifications
6. WHEN creating resources, THE API_Compatibility_Layer SHALL automatically associate them with the authenticated tenant
7. THE API_Compatibility_Layer SHALL prevent tenant ID manipulation in request bodies
8. THE API_Compatibility_Layer SHALL log tenant isolation violations for security monitoring
9. THE API_Compatibility_Layer SHALL use the same tenant isolation strategy as Node_Backend
10. THE API_Compatibility_Layer SHALL ensure tenant ID is never exposed in error messages

### Requirement 14: Pagination Response Compatibility

**User Story:** As a frontend developer, I want paginated responses to have the same structure as Node.js, so that pagination UI components work without modification.

#### Acceptance Criteria

1. WHEN listing resources with pagination, THE Response_DTO SHALL include "data" array containing the resources
2. THE Response_DTO SHALL include "page" field indicating current page number
3. THE Response_DTO SHALL include "size" field indicating page size
4. THE Response_DTO SHALL include "total" field indicating total number of resources
5. THE Response_DTO SHALL include "totalPages" field indicating total number of pages
6. THE API_Compatibility_Layer SHALL use zero-based page numbering matching Node_Backend
7. WHEN page parameter exceeds total pages, THE API_Compatibility_Layer SHALL return empty data array
8. WHEN size parameter is invalid, THE API_Compatibility_Layer SHALL use default page size of 10
9. THE API_Compatibility_Layer SHALL limit maximum page size to 100 items
10. THE Response_DTO SHALL maintain the same field order as Node_Backend pagination responses

### Requirement 15: Workflow BPMN XML Handling Compatibility

**User Story:** As a frontend developer, I want BPMN XML workflow definitions to be handled identically to Node.js, so that the workflow designer integration remains unchanged.

#### Acceptance Criteria

1. WHEN creating a workflow, THE API_Compatibility_Layer SHALL accept BPMN XML in "bpmnXml" field
2. WHEN retrieving a workflow, THE Response_DTO SHALL include "bpmnXml" field containing the BPMN XML
3. THE API_Compatibility_Layer SHALL preserve BPMN XML formatting and whitespace
4. THE API_Compatibility_Layer SHALL validate BPMN XML structure before saving
5. WHEN BPMN XML is invalid, THE API_Compatibility_Layer SHALL return HTTP 400 Bad Request with validation details
6. THE API_Compatibility_Layer SHALL handle BPMN XML documents up to 5MB in size
7. THE API_Compatibility_Layer SHALL escape special characters in BPMN XML for JSON serialization
8. THE API_Compatibility_Layer SHALL store BPMN XML as TEXT column type in database
9. WHEN BPMN XML contains UTF-8 characters, THE API_Compatibility_Layer SHALL preserve encoding
10. THE API_Compatibility_Layer SHALL use the same BPMN XML field naming as Node_Backend

