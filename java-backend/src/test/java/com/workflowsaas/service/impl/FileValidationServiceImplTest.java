package com.workflowsaas.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowsaas.exception.FileValidationException;
import com.workflowsaas.service.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileValidationServiceImpl.
 */
class FileValidationServiceImplTest {
    
    private FileValidationService fileValidationService;
    
    @BeforeEach
    void setUp() {
        fileValidationService = new FileValidationServiceImpl(new ObjectMapper());
    }
    
    @Test
    void validateOpenApiDocument_withValidDocument_shouldPass() {
        String validOpenApi = """
            {
                "openapi": "3.0.0",
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                },
                "paths": {
                    "/test": {
                        "get": {
                            "summary": "Test endpoint"
                        }
                    }
                }
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateOpenApiDocument(validOpenApi));
    }
    
    @Test
    void validateOpenApiDocument_withVersion3_1_0_shouldPass() {
        String validOpenApi = """
            {
                "openapi": "3.1.0",
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                },
                "paths": {}
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateOpenApiDocument(validOpenApi));
    }
    
    @Test
    void validateOpenApiDocument_withNullContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(null)
        );
        
        assertEquals("OpenAPI document content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateOpenApiDocument_withEmptyContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument("   ")
        );
        
        assertEquals("OpenAPI document content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateOpenApiDocument_withInvalidJson_shouldThrowExceptionWithLocation() {
        String invalidJson = """
            {
                "openapi": "3.0.0",
                "info": {
                    "title": "Test API"
                },
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(invalidJson)
        );
        
        assertTrue(exception.getMessage().contains("Invalid JSON"));
        assertTrue(exception.getMessage().contains("line"));
        assertTrue(exception.getMessage().contains("column"));
    }
    
    @Test
    void validateOpenApiDocument_withMissingOpenapiField_shouldThrowException() {
        String missingOpenapi = """
            {
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                },
                "paths": {}
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(missingOpenapi)
        );
        
        assertEquals("Invalid OpenAPI document: Missing required field 'openapi' in root object", exception.getMessage());
    }
    
    @Test
    void validateOpenApiDocument_withMissingInfoField_shouldThrowException() {
        String missingInfo = """
            {
                "openapi": "3.0.0",
                "paths": {}
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(missingInfo)
        );
        
        assertEquals("Invalid OpenAPI document: Missing required field 'info' in root object", exception.getMessage());
    }
    
    @Test
    void validateOpenApiDocument_withMissingPathsField_shouldThrowException() {
        String missingPaths = """
            {
                "openapi": "3.0.0",
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                }
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(missingPaths)
        );
        
        assertEquals("Invalid OpenAPI document: Missing required field 'paths' in root object", exception.getMessage());
    }
    
    @Test
    void validateOpenApiDocument_withInvalidVersionFormat_shouldThrowException() {
        String invalidVersion = """
            {
                "openapi": "2.0.0",
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                },
                "paths": {}
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(invalidVersion)
        );
        
        assertTrue(exception.getMessage().contains("Version must be in format '3.x.x'"));
        assertTrue(exception.getMessage().contains("2.0.0"));
    }
    
    @Test
    void validateOpenApiDocument_withNonNumericMinorVersion_shouldThrowException() {
        String invalidVersion = """
            {
                "openapi": "3.x.0",
                "info": {
                    "title": "Test API",
                    "version": "1.0.0"
                },
                "paths": {}
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateOpenApiDocument(invalidVersion)
        );
        
        assertTrue(exception.getMessage().contains("Version must be in format '3.x.x'"));
    }
    
    // Avro Schema Validation Tests
    
    @Test
    void validateAvroSchema_withValidRecordSchema_shouldPass() {
        String validAvroSchema = """
            {
                "type": "record",
                "name": "User",
                "fields": [
                    {"name": "id", "type": "long"},
                    {"name": "username", "type": "string"}
                ]
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(validAvroSchema));
    }
    
    @Test
    void validateAvroSchema_withValidPrimitiveSchema_shouldPass() {
        String validAvroSchema = """
            {
                "type": "string",
                "name": "SimpleString"
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(validAvroSchema));
    }
    
    @Test
    void validateAvroSchema_withValidEnumSchema_shouldPass() {
        String validAvroSchema = """
            {
                "type": "enum",
                "name": "Status",
                "symbols": ["ACTIVE", "INACTIVE"]
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(validAvroSchema));
    }
    
    @Test
    void validateAvroSchema_withValidArraySchema_shouldPass() {
        String validAvroSchema = """
            {
                "type": "array",
                "name": "StringArray",
                "items": "string"
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(validAvroSchema));
    }
    
    @Test
    void validateAvroSchema_withValidMapSchema_shouldPass() {
        String validAvroSchema = """
            {
                "type": "map",
                "name": "StringMap",
                "values": "string"
            }
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(validAvroSchema));
    }
    
    @Test
    void validateAvroSchema_withNullContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(null)
        );
        
        assertEquals("Avro schema content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withEmptyContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema("   ")
        );
        
        assertEquals("Avro schema content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withInvalidJson_shouldThrowExceptionWithLocation() {
        String invalidJson = """
            {
                "type": "record",
                "name": "User",
                "fields": [
                    {"name": "id", "type": "long"},
                ]
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(invalidJson)
        );
        
        assertTrue(exception.getMessage().contains("Invalid JSON"));
        assertTrue(exception.getMessage().contains("line"));
        assertTrue(exception.getMessage().contains("column"));
    }
    
    @Test
    void validateAvroSchema_withMissingNameField_shouldThrowException() {
        String missingName = """
            {
                "type": "record",
                "fields": []
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(missingName)
        );
        
        assertEquals("Invalid Avro schema: Missing required field 'name'", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withMissingTypeField_shouldThrowException() {
        String missingType = """
            {
                "name": "User",
                "fields": []
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(missingType)
        );
        
        assertEquals("Invalid Avro schema: Missing required field 'type'", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withInvalidType_shouldThrowException() {
        String invalidType = """
            {
                "type": "invalid_type",
                "name": "User"
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(invalidType)
        );
        
        assertTrue(exception.getMessage().contains("Field 'type' must be one of"));
        assertTrue(exception.getMessage().contains("invalid_type"));
    }
    
    @Test
    void validateAvroSchema_withRecordTypeMissingFields_shouldThrowException() {
        String recordWithoutFields = """
            {
                "type": "record",
                "name": "User"
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(recordWithoutFields)
        );
        
        assertEquals("Invalid Avro schema: Record type requires 'fields' array", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withRecordTypeFieldsNotArray_shouldThrowException() {
        String recordWithInvalidFields = """
            {
                "type": "record",
                "name": "User",
                "fields": "not_an_array"
            }
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateAvroSchema(recordWithInvalidFields)
        );
        
        assertEquals("Invalid Avro schema: 'fields' must be an array", exception.getMessage());
    }
    
    @Test
    void validateAvroSchema_withAllPrimitiveTypes_shouldPass() {
        String[] primitiveTypes = {"null", "boolean", "int", "long", "float", "double", "bytes", "string"};
        
        for (String type : primitiveTypes) {
            String schema = String.format("""
                {
                    "type": "%s",
                    "name": "Test%s"
                }
                """, type, type);
            
            assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(schema),
                "Should accept primitive type: " + type);
        }
    }
    
    @Test
    void validateAvroSchema_withAllComplexTypes_shouldPass() {
        String[] complexTypes = {"record", "enum", "array", "map", "union", "fixed"};
        
        for (String type : complexTypes) {
            String schema;
            if ("record".equals(type)) {
                schema = String.format("""
                    {
                        "type": "%s",
                        "name": "Test%s",
                        "fields": []
                    }
                    """, type, type);
            } else {
                schema = String.format("""
                    {
                        "type": "%s",
                        "name": "Test%s"
                    }
                    """, type, type);
            }
            
            assertDoesNotThrow(() -> fileValidationService.validateAvroSchema(schema),
                "Should accept complex type: " + type);
        }
    }
    
    // CSV Validation Tests
    
    @Test
    void validateCsvContent_withValidSimpleCsv_shouldPass() {
        String validCsv = """
            name,age,city
            John,30,NYC
            Jane,25,LA
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withValidSingleRow_shouldPass() {
        String validCsv = "name,age,city";
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withValidQuotedFields_shouldPass() {
        String validCsv = """
            name,description,price
            "Product A","A great product",100
            "Product B","Another product with, comma",200
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withValidEscapedQuotes_shouldPass() {
        String validCsv = "name,description\n" +
            "\"Product A\",\"Description with \"\"quotes\"\"\"\n" +
            "\"Product B\",\"Normal description\"";
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withValidEmptyFields_shouldPass() {
        String validCsv = """
            name,age,city
            John,,NYC
            ,25,LA
            Jane,30,
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withValidSingleColumn_shouldPass() {
        String validCsv = """
            name
            John
            Jane
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withNullContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent(null)
        );
        
        assertEquals("CSV content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateCsvContent_withEmptyContent_shouldThrowException() {
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent("   ")
        );
        
        assertEquals("CSV content cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateCsvContent_withInconsistentColumnCount_shouldThrowException() {
        String invalidCsv = """
            name,age,city
            John,30,NYC
            Jane,25
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent(invalidCsv)
        );
        
        assertTrue(exception.getMessage().contains("Invalid CSV structure at row 3"));
        assertTrue(exception.getMessage().contains("Expected 3 columns but found 2"));
    }
    
    @Test
    void validateCsvContent_withExtraColumns_shouldThrowException() {
        String invalidCsv = """
            name,age
            John,30
            Jane,25,LA,Extra
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent(invalidCsv)
        );
        
        assertTrue(exception.getMessage().contains("Invalid CSV structure at row 3"));
        assertTrue(exception.getMessage().contains("Expected 2 columns but found 4"));
    }
    
    @Test
    void validateCsvContent_withUnclosedQuote_shouldThrowException() {
        String invalidCsv = """
            name,description
            "Product A","Unclosed quote
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent(invalidCsv)
        );
        
        assertTrue(exception.getMessage().contains("Invalid CSV structure at row 2"));
        assertTrue(exception.getMessage().contains("Missing closing quote"));
    }
    
    @Test
    void validateCsvContent_withUnclosedQuoteInMiddle_shouldThrowException() {
        String invalidCsv = """
            name,description,price
            "Product A","Description with unclosed quote,100
            """;
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateCsvContent(invalidCsv)
        );
        
        assertTrue(exception.getMessage().contains("Invalid CSV structure at row 2"));
        assertTrue(exception.getMessage().contains("Missing closing quote"));
    }
    
    @Test
    void validateCsvContent_withEmptyLinesInMiddle_shouldPass() {
        String validCsv = """
            name,age,city
            John,30,NYC
            
            Jane,25,LA
            """;
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withWindowsLineEndings_shouldPass() {
        String validCsv = "name,age,city\r\nJohn,30,NYC\r\nJane,25,LA";
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    @Test
    void validateCsvContent_withMixedLineEndings_shouldPass() {
        String validCsv = "name,age,city\nJohn,30,NYC\r\nJane,25,LA";
        
        assertDoesNotThrow(() -> fileValidationService.validateCsvContent(validCsv));
    }
    
    // Request Mappings Validation Tests
    
    @Test
    void validateRequestMappings_withValidSingleMapping_shouldPass() {
        String validMapping = "source -> target";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withValidMultipleMappings_shouldPass() {
        String validMappings = "source1 -> target1 | source2 -> target2 | source3 -> target3";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMappings));
    }
    
    @Test
    void validateRequestMappings_withValidFieldNamesWithDots_shouldPass() {
        String validMapping = "user.name -> customer.fullName";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withValidFieldNamesWithUnderscores_shouldPass() {
        String validMapping = "user_name -> customer_full_name";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withValidFieldNamesWithNumbers_shouldPass() {
        String validMapping = "field1 -> field2";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withValidMixedCharacters_shouldPass() {
        String validMapping = "user_name.first123 -> customer.name_1";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withExtraWhitespace_shouldPass() {
        String validMapping = "  source  ->  target  ";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMapping));
    }
    
    @Test
    void validateRequestMappings_withMultipleMappingsAndWhitespace_shouldPass() {
        String validMappings = " source1 -> target1  |  source2 -> target2 ";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMappings));
    }
    
    @Test
    void validateRequestMappings_withNullContent_shouldPass() {
        // Null or empty mappings are allowed
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(null));
    }
    
    @Test
    void validateRequestMappings_withEmptyContent_shouldPass() {
        // Null or empty mappings are allowed
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings("   "));
    }
    
    @Test
    void validateRequestMappings_withEmptyString_shouldPass() {
        // Null or empty mappings are allowed
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(""));
    }
    
    @Test
    void validateRequestMappings_withMissingArrow_shouldThrowException() {
        String invalidMapping = "source target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Request mapping must follow format 'source -> target'"));
        assertTrue(exception.getMessage().contains("source target"));
    }
    
    @Test
    void validateRequestMappings_withMultipleArrows_shouldThrowException() {
        String invalidMapping = "source -> middle -> target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Request mapping must follow format 'source -> target'"));
    }
    
    @Test
    void validateRequestMappings_withEmptySource_shouldThrowException() {
        String invalidMapping = " -> target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Source field name cannot be empty"));
    }
    
    @Test
    void validateRequestMappings_withEmptyTarget_shouldThrowException() {
        String invalidMapping = "source -> ";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Target field name cannot be empty"));
    }
    
    @Test
    void validateRequestMappings_withInvalidCharactersInSource_shouldThrowException() {
        String invalidMapping = "user-name -> target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Field name 'user-name' contains invalid characters"));
        assertTrue(exception.getMessage().contains("Only alphanumeric, dots, and underscores are allowed"));
    }
    
    @Test
    void validateRequestMappings_withInvalidCharactersInTarget_shouldThrowException() {
        String invalidMapping = "source -> user-name";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Field name 'user-name' contains invalid characters"));
        assertTrue(exception.getMessage().contains("Only alphanumeric, dots, and underscores are allowed"));
    }
    
    @Test
    void validateRequestMappings_withSpacesInFieldName_shouldThrowException() {
        String invalidMapping = "user name -> target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Field name 'user name' contains invalid characters"));
    }
    
    @Test
    void validateRequestMappings_withSpecialCharactersInFieldName_shouldThrowException() {
        String invalidMapping = "user@name -> target";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMapping)
        );
        
        assertTrue(exception.getMessage().contains("Field name 'user@name' contains invalid characters"));
    }
    
    @Test
    void validateRequestMappings_withMultipleMappingsOneInvalid_shouldThrowException() {
        String invalidMappings = "source1 -> target1 | invalid-source -> target2";
        
        FileValidationException exception = assertThrows(
            FileValidationException.class,
            () -> fileValidationService.validateRequestMappings(invalidMappings)
        );
        
        assertTrue(exception.getMessage().contains("Field name 'invalid-source' contains invalid characters"));
    }
    
    @Test
    void validateRequestMappings_withEmptyPipeEntry_shouldPass() {
        // Empty entries between pipes should be skipped
        String validMappings = "source1 -> target1 | | source2 -> target2";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMappings));
    }
    
    @Test
    void validateRequestMappings_withTrailingPipe_shouldPass() {
        String validMappings = "source1 -> target1 | source2 -> target2 |";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMappings));
    }
    
    @Test
    void validateRequestMappings_withLeadingPipe_shouldPass() {
        String validMappings = "| source1 -> target1 | source2 -> target2";
        
        assertDoesNotThrow(() -> fileValidationService.validateRequestMappings(validMappings));
    }
}
