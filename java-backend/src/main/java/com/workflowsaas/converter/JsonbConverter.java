package com.workflowsaas.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HashMap;
import java.util.Map;

/**
 * JPA AttributeConverter for converting Map<String, String> to JSONB format in PostgreSQL.
 * This converter is used by EnrichmentApiConfig and PublisherConfig entities to store
 * config_properties as JSONB in the database.
 * 
 * Handles null and empty map cases gracefully:
 * - null maps are stored as null in the database
 * - empty maps are stored as null in the database
 * - null database values are converted to empty maps
 */
@Converter
public class JsonbConverter implements AttributeConverter<Map<String, String>, String> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Converts a Map<String, String> to a JSON string for database storage.
     * 
     * @param attribute the map to convert (may be null or empty)
     * @return JSON string representation, or null if the map is null or empty
     * @throws IllegalArgumentException if JSON serialization fails
     */
    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }
    
    /**
     * Converts a JSON string from the database to a Map<String, String>.
     * 
     * @param dbData the JSON string from the database (may be null or empty)
     * @return Map<String, String> representation, or empty map if dbData is null or empty
     * @throws IllegalArgumentException if JSON deserialization fails
     */
    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
