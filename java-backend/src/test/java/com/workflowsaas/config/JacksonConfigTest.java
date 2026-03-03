package com.workflowsaas.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JacksonConfig.
 * Validates that ObjectMapper is configured with increased string length limits.
 */
class JacksonConfigTest {

    @Test
    void testObjectMapperConfiguration() {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // Act
        ObjectMapper mapper = config.objectMapper(builder);

        // Assert
        assertNotNull(mapper, "ObjectMapper should not be null");
        assertNotNull(mapper.getFactory(), "ObjectMapper factory should not be null");
    }

    @Test
    void testStreamReadConstraintsConfiguration() {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // Act
        ObjectMapper mapper = config.objectMapper(builder);
        StreamReadConstraints constraints = mapper.getFactory().streamReadConstraints();

        // Assert
        assertNotNull(constraints, "StreamReadConstraints should not be null");
        assertEquals(50_000_000, constraints.getMaxStringLength(), 
                "Max string length should be 50MB (50,000,000 bytes)");
    }

    @Test
    void testDefaultMaxStringLengthIsIncreased() {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        
        // Default ObjectMapper for comparison
        ObjectMapper defaultMapper = new ObjectMapper();
        int defaultMaxLength = defaultMapper.getFactory().streamReadConstraints().getMaxStringLength();

        // Act
        ObjectMapper configuredMapper = config.objectMapper(builder);
        int configuredMaxLength = configuredMapper.getFactory().streamReadConstraints().getMaxStringLength();

        // Assert
        assertTrue(configuredMaxLength > defaultMaxLength, 
                "Configured max string length should be greater than default");
        assertEquals(50_000_000, configuredMaxLength, 
                "Configured max string length should be 50MB");
    }

    @Test
    void testObjectMapperCanHandleLargeStrings() throws Exception {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        ObjectMapper mapper = config.objectMapper(builder);

        // Create a large string (1MB)
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 1_000_000; i++) {
            largeString.append("a");
        }
        String testData = "{\"data\":\"" + largeString.toString() + "\"}";

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            mapper.readTree(testData);
        }, "ObjectMapper should handle 1MB strings without throwing exception");
    }

    @Test
    void testObjectMapperCanSerializeAndDeserialize() throws Exception {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        ObjectMapper mapper = config.objectMapper(builder);

        TestObject original = new TestObject("test", 123);

        // Act
        String json = mapper.writeValueAsString(original);
        TestObject deserialized = mapper.readValue(json, TestObject.class);

        // Assert
        assertNotNull(json, "Serialized JSON should not be null");
        assertNotNull(deserialized, "Deserialized object should not be null");
        assertEquals(original.name, deserialized.name, "Name should match");
        assertEquals(original.value, deserialized.value, "Value should match");
    }

    @Test
    void testMultipleObjectMapperInstancesHaveSameConfiguration() {
        // Arrange
        JacksonConfig config = new JacksonConfig();
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // Act
        ObjectMapper mapper1 = config.objectMapper(builder);
        ObjectMapper mapper2 = config.objectMapper(builder);

        // Assert
        assertEquals(
                mapper1.getFactory().streamReadConstraints().getMaxStringLength(),
                mapper2.getFactory().streamReadConstraints().getMaxStringLength(),
                "Both ObjectMapper instances should have the same max string length"
        );
    }

    /**
     * Test object for serialization/deserialization tests.
     */
    static class TestObject {
        public String name;
        public int value;

        public TestObject() {
        }

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
