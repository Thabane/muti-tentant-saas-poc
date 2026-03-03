package com.workflowsaas.service;

/**
 * Service for validating uploaded configuration files.
 * Supports validation of OpenAPI documents, Avro schemas, CSV files, and request mappings.
 */
public interface FileValidationService {
    
    /**
     * Validates an OpenAPI document in JSON format.
     * Checks for required fields (openapi, info, paths) and validates version format.
     * 
     * @param jsonContent the OpenAPI document content as JSON string
     * @throws com.workflowsaas.exception.FileValidationException if validation fails
     */
    void validateOpenApiDocument(String jsonContent);
    
    /**
     * Validates an Avro schema in JSON format.
     * Checks for required fields (name, type, fields for record type) and validates type values.
     * 
     * @param jsonContent the Avro schema content as JSON string
     * @throws com.workflowsaas.exception.FileValidationException if validation fails
     */
    void validateAvroSchema(String jsonContent);
    
    /**
     * Validates CSV file content.
     * Checks for consistent column count per row and proper quote handling.
     * 
     * @param csvContent the CSV file content as string
     * @throws com.workflowsaas.exception.FileValidationException if validation fails
     */
    void validateCsvContent(String csvContent);
    
    /**
     * Validates request mappings format.
     * Expected format: "source -> target" with optional pipe separators for multiple mappings.
     * Field names must contain only alphanumeric characters, dots, and underscores.
     * 
     * @param mappings the request mappings string
     * @throws com.workflowsaas.exception.FileValidationException if validation fails
     */
    void validateRequestMappings(String mappings);
}
