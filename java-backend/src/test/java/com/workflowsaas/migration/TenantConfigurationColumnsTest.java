package com.workflowsaas.migration;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for tenant configuration columns migration file.
 * Validates that Liquibase migration 007 YAML file is correctly structured
 * and contains the required column definitions.
 * 
 * Note: This test validates the migration file structure, not the actual
 * database changes. The migration is tested in production with PostgreSQL.
 */
class TenantConfigurationColumnsTest {

    @Test
    void testMigrationFileStructureAndContent() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/007-add-tenant-configuration-columns.yaml");
        
        assertNotNull(inputStream, "Migration file 007-add-tenant-configuration-columns.yaml should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        assertNotNull(data, "YAML file should be parseable");
        assertTrue(data.containsKey("databaseChangeLog"), "Should have databaseChangeLog root element");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        assertNotNull(changeSets, "Should have changeSets");
        assertFalse(changeSets.isEmpty(), "Should have at least one changeSet");
        
        Map<String, Object> changeSet = changeSets.get(0);
        assertTrue(changeSet.containsKey("changeSet"), "Should have changeSet element");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSet.get("changeSet");
        
        assertEquals("007-add-tenant-configuration-columns", changeSetDetails.get("id"), 
                "ChangeSet ID should be 007-add-tenant-configuration-columns");
        assertEquals("system", changeSetDetails.get("author"), "Author should be system");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changes = (List<Map<String, Object>>) changeSetDetails.get("changes");
        
        assertNotNull(changes, "Should have changes");
        assertFalse(changes.isEmpty(), "Should have at least one change");
        
        Map<String, Object> addColumnChange = changes.get(0);
        assertTrue(addColumnChange.containsKey("addColumn"), "Should have addColumn change");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> addColumn = (Map<String, Object>) addColumnChange.get("addColumn");
        
        assertEquals("tenants", addColumn.get("tableName"), "Should target tenants table");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) addColumn.get("columns");
        
        assertNotNull(columns, "Should have columns");
        assertEquals(1, columns.size(), "Should have 1 column");
        
        // Validate configuration column
        @SuppressWarnings("unchecked")
        Map<String, Object> configColumn = (Map<String, Object>) columns.get(0).get("column");
        
        assertEquals("configuration", configColumn.get("name"), "First column should be configuration");
        assertEquals("jsonb", configColumn.get("type"), "configuration should be jsonb type");
        assertEquals("'{}'::jsonb", configColumn.get("defaultValueComputed"), 
                "configuration should default to empty JSON object");
    }
}
