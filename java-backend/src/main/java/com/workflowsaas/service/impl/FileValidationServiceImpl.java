package com.workflowsaas.service.impl;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowsaas.exception.FileValidationException;
import com.workflowsaas.service.FileValidationService;
import org.springframework.stereotype.Service;

/**
 * Implementation of FileValidationService for validating uploaded configuration files.
 * Package-private visibility following Spring Boot best practices.
 */
@Service
class FileValidationServiceImpl implements FileValidationService {
    
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor injection for ObjectMapper dependency.
     * 
     * @param objectMapper Jackson ObjectMapper for JSON parsing
     */
    FileValidationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void validateOpenApiDocument(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new FileValidationException("OpenAPI document content cannot be empty");
        }
        
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonContent);
        } catch (JsonProcessingException e) {
            JsonLocation location = e.getLocation();
            String locationInfo = location != null 
                ? String.format(" at line %d, column %d", location.getLineNr(), location.getColumnNr())
                : "";
            throw new FileValidationException(
                "Invalid JSON" + locationInfo + ": " + e.getOriginalMessage(), 
                e
            );
        }
        
        // Validate required fields
        if (!root.has("openapi")) {
            throw new FileValidationException("Invalid OpenAPI document: Missing required field 'openapi' in root object");
        }
        
        if (!root.has("info")) {
            throw new FileValidationException("Invalid OpenAPI document: Missing required field 'info' in root object");
        }
        
        if (!root.has("paths")) {
            throw new FileValidationException("Invalid OpenAPI document: Missing required field 'paths' in root object");
        }
        
        // Validate openapi version format (3.x.x)
        String version = root.get("openapi").asText();
        if (!version.matches("^3\\.\\d+\\.\\d+$")) {
            throw new FileValidationException(
                "Invalid OpenAPI document: Version must be in format '3.x.x', found: '" + version + "'"
            );
        }
    }
    
    @Override
    public void validateAvroSchema(String jsonContent) {
        if (jsonContent == null || jsonContent.trim().isEmpty()) {
            throw new FileValidationException("Avro schema content cannot be empty");
        }
        
        JsonNode root;
        try {
            root = objectMapper.readTree(jsonContent);
        } catch (JsonProcessingException e) {
            JsonLocation location = e.getLocation();
            String locationInfo = location != null 
                ? String.format(" at line %d, column %d", location.getLineNr(), location.getColumnNr())
                : "";
            throw new FileValidationException(
                "Invalid JSON" + locationInfo + ": " + e.getOriginalMessage(), 
                e
            );
        }
        
        // Validate required fields
        if (!root.has("name")) {
            throw new FileValidationException("Invalid Avro schema: Missing required field 'name'");
        }
        
        if (!root.has("type")) {
            throw new FileValidationException("Invalid Avro schema: Missing required field 'type'");
        }
        
        // Validate type value
        String type = root.get("type").asText();
        if (!isValidAvroType(type)) {
            throw new FileValidationException(
                "Invalid Avro schema: Field 'type' must be one of [null, boolean, int, long, float, double, bytes, string, record, enum, array, map, union, fixed], found: '" + type + "'"
            );
        }
        
        // If type is "record", validate that "fields" array exists
        if ("record".equals(type)) {
            if (!root.has("fields")) {
                throw new FileValidationException("Invalid Avro schema: Record type requires 'fields' array");
            }
            
            if (!root.get("fields").isArray()) {
                throw new FileValidationException("Invalid Avro schema: 'fields' must be an array");
            }
        }
    }
    
    /**
     * Checks if the given type is a valid Avro type.
     * 
     * @param type the type string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidAvroType(String type) {
        return type != null && (
            type.equals("null") ||
            type.equals("boolean") ||
            type.equals("int") ||
            type.equals("long") ||
            type.equals("float") ||
            type.equals("double") ||
            type.equals("bytes") ||
            type.equals("string") ||
            type.equals("record") ||
            type.equals("enum") ||
            type.equals("array") ||
            type.equals("map") ||
            type.equals("union") ||
            type.equals("fixed")
        );
    }
    
    @Override
    public void validateCsvContent(String csvContent) {
        if (csvContent == null || csvContent.trim().isEmpty()) {
            throw new FileValidationException("CSV content cannot be empty");
        }
        
        String[] lines = csvContent.split("\\r?\\n");
        if (lines.length == 0) {
            throw new FileValidationException("CSV file must contain at least one row");
        }
        
        Integer expectedColumnCount = null;
        
        for (int rowIndex = 0; rowIndex < lines.length; rowIndex++) {
            String line = lines[rowIndex];
            
            // Skip empty lines
            if (line.trim().isEmpty()) {
                continue;
            }
            
            // Parse CSV line with quote handling
            int columnCount = 0;
            boolean inQuotes = false;
            int columnStart = 0;
            
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                
                if (c == '"') {
                    // Check for escaped quote (double quote)
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        i++; // Skip the escaped quote
                    } else {
                        inQuotes = !inQuotes;
                    }
                } else if (c == ',' && !inQuotes) {
                    columnCount++;
                    columnStart = i + 1;
                }
            }
            
            // Check for unclosed quotes
            if (inQuotes) {
                throw new FileValidationException(
                    String.format("Invalid CSV structure at row %d, column %d: Missing closing quote",
                        rowIndex + 1, columnStart + 1)
                );
            }
            
            // Add the last column
            columnCount++;
            
            // Validate consistent column count
            if (expectedColumnCount == null) {
                expectedColumnCount = columnCount;
            } else if (columnCount != expectedColumnCount) {
                throw new FileValidationException(
                    String.format("Invalid CSV structure at row %d: Expected %d columns but found %d",
                        rowIndex + 1, expectedColumnCount, columnCount)
                );
            }
        }
    }
    
    @Override
    public void validateRequestMappings(String mappings) {
        if (mappings == null || mappings.trim().isEmpty()) {
            // Empty mappings are allowed
            return;
        }
        
        // Split by pipe character to handle multiple mappings
        String[] mappingEntries = mappings.split("\\|");
        
        for (String entry : mappingEntries) {
            String trimmedEntry = entry.trim();
            
            // Skip empty entries
            if (trimmedEntry.isEmpty()) {
                continue;
            }
            
            // Validate format: "source -> target"
            if (!trimmedEntry.contains("->")) {
                throw new FileValidationException(
                    "Request mapping must follow format 'source -> target', found: '" + trimmedEntry + "'"
                );
            }
            
            // Split by arrow (use -1 to preserve trailing empty strings)
            String[] parts = trimmedEntry.split("->", -1);
            
            if (parts.length != 2) {
                throw new FileValidationException(
                    "Request mapping must follow format 'source -> target', found: '" + trimmedEntry + "'"
                );
            }
            
            String source = parts[0].trim();
            String target = parts[1].trim();
            
            // Validate source and target are not empty
            if (source.isEmpty()) {
                throw new FileValidationException(
                    "Source field name cannot be empty in mapping: '" + trimmedEntry + "'"
                );
            }
            
            if (target.isEmpty()) {
                throw new FileValidationException(
                    "Target field name cannot be empty in mapping: '" + trimmedEntry + "'"
                );
            }
            
            // Validate field names contain only alphanumeric, dots, and underscores
            if (!isValidFieldName(source)) {
                throw new FileValidationException(
                    "Field name '" + source + "' contains invalid characters. Only alphanumeric, dots, and underscores are allowed"
                );
            }
            
            if (!isValidFieldName(target)) {
                throw new FileValidationException(
                    "Field name '" + target + "' contains invalid characters. Only alphanumeric, dots, and underscores are allowed"
                );
            }
        }
    }
    
    /**
     * Checks if the given field name contains only alphanumeric characters, dots, and underscores.
     * 
     * @param fieldName the field name to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidFieldName(String fieldName) {
        return fieldName != null && fieldName.matches("^[a-zA-Z0-9._]+$");
    }
}
