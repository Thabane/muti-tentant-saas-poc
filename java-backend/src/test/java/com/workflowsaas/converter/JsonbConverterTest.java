package com.workflowsaas.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonbConverter.
 * Tests conversion between Map<String, String> and JSON string format.
 */
class JsonbConverterTest {
    
    private JsonbConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new JsonbConverter();
    }
    
    @Test
    void convertToDatabaseColumn_withValidMap_returnsJsonString() {
        // Given
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        // When
        String json = converter.convertToDatabaseColumn(map);
        
        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"key1\""));
        assertTrue(json.contains("\"value1\""));
        assertTrue(json.contains("\"key2\""));
        assertTrue(json.contains("\"value2\""));
    }
    
    @Test
    void convertToDatabaseColumn_withNullMap_returnsNull() {
        // When
        String json = converter.convertToDatabaseColumn(null);
        
        // Then
        assertNull(json);
    }
    
    @Test
    void convertToDatabaseColumn_withEmptyMap_returnsNull() {
        // Given
        Map<String, String> emptyMap = new HashMap<>();
        
        // When
        String json = converter.convertToDatabaseColumn(emptyMap);
        
        // Then
        assertNull(json);
    }
    
    @Test
    void convertToEntityAttribute_withValidJson_returnsMap() {
        // Given
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        
        // When
        Map<String, String> map = converter.convertToEntityAttribute(json);
        
        // Then
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }
    
    @Test
    void convertToEntityAttribute_withNullJson_returnsEmptyMap() {
        // When
        Map<String, String> map = converter.convertToEntityAttribute(null);
        
        // Then
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }
    
    @Test
    void convertToEntityAttribute_withEmptyJson_returnsEmptyMap() {
        // When
        Map<String, String> map = converter.convertToEntityAttribute("");
        
        // Then
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }
    
    @Test
    void convertToEntityAttribute_withInvalidJson_throwsException() {
        // Given
        String invalidJson = "{invalid json}";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            converter.convertToEntityAttribute(invalidJson)
        );
    }
    
    @Test
    void roundTrip_preservesMapData() {
        // Given
        Map<String, String> originalMap = new HashMap<>();
        originalMap.put("host", "localhost");
        originalMap.put("port", "8080");
        originalMap.put("timeout", "30");
        
        // When
        String json = converter.convertToDatabaseColumn(originalMap);
        Map<String, String> resultMap = converter.convertToEntityAttribute(json);
        
        // Then
        assertEquals(originalMap, resultMap);
    }
    
    @Test
    void convertToDatabaseColumn_withSpecialCharacters_handlesCorrectly() {
        // Given
        Map<String, String> map = new HashMap<>();
        map.put("url", "https://api.example.com/v1");
        map.put("description", "Test with \"quotes\" and \\ backslash");
        
        // When
        String json = converter.convertToDatabaseColumn(map);
        Map<String, String> resultMap = converter.convertToEntityAttribute(json);
        
        // Then
        assertEquals(map, resultMap);
    }
}
