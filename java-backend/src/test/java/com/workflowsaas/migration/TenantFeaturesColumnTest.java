package com.workflowsaas.migration;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for tenant features columns migration file.
 * Validates that Liquibase migration 003 YAML file is correctly structured
 * and contains the required column definitions with proper preconditions.
 * 
 * Note: This test validates the migration file structure, not the actual
 * database changes. The migration is tested in production with PostgreSQL.
 */
class TenantFeaturesColumnTest {

    @Test
    void testMigrationFileExists() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file 003-add-tenant-features.yaml should exist");
    }

    @Test
    void testMigrationFileStructureAndContent() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
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
        
        assertEquals("003-add-tenant-features", changeSetDetails.get("id"), 
                "ChangeSet ID should be 003-add-tenant-features");
        assertEquals("system", changeSetDetails.get("author"), "Author should be system");
    }

    @Test
    void testPreConditionsExist() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
        assertTrue(changeSetDetails.containsKey("preConditions"), 
                "ChangeSet should have preConditions");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preConditions = (List<Map<String, Object>>) changeSetDetails.get("preConditions");
        
        assertNotNull(preConditions, "PreConditions should not be null");
        assertFalse(preConditions.isEmpty(), "PreConditions should not be empty");
        
        // Verify onFail: MARK_RAN
        boolean hasOnFail = preConditions.stream()
                .anyMatch(pc -> pc.containsKey("onFail") && "MARK_RAN".equals(pc.get("onFail")));
        assertTrue(hasOnFail, "Should have onFail: MARK_RAN precondition");
        
        // Verify not -> columnExists precondition
        boolean hasColumnExistsCheck = preConditions.stream()
                .anyMatch(pc -> {
                    if (pc.containsKey("not")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> notConditions = (List<Map<String, Object>>) pc.get("not");
                        return notConditions.stream()
                                .anyMatch(nc -> nc.containsKey("columnExists"));
                    }
                    return false;
                });
        assertTrue(hasColumnExistsCheck, "Should have 'not -> columnExists' precondition");
    }

    @Test
    void testColumnExistsPreconditionDetails() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preConditions = (List<Map<String, Object>>) changeSetDetails.get("preConditions");
        
        // Find the 'not' precondition
        Map<String, Object> notPrecondition = preConditions.stream()
                .filter(pc -> pc.containsKey("not"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(notPrecondition, "Should have 'not' precondition");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> notConditions = (List<Map<String, Object>>) notPrecondition.get("not");
        
        Map<String, Object> columnExistsCondition = notConditions.stream()
                .filter(nc -> nc.containsKey("columnExists"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(columnExistsCondition, "Should have columnExists condition");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> columnExists = (Map<String, Object>) columnExistsCondition.get("columnExists");
        
        assertEquals("tenants", columnExists.get("tableName"), 
                "Should check tenants table");
        assertEquals("features", columnExists.get("columnName"), 
                "Should check features column");
    }

    @Test
    void testAddColumnChanges() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
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
        assertEquals(2, columns.size(), "Should have 2 columns");
    }

    @Test
    void testFeaturesColumnDefinition() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changes = (List<Map<String, Object>>) changeSetDetails.get("changes");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> addColumn = (Map<String, Object>) changes.get(0).get("addColumn");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) addColumn.get("columns");
        
        // Validate features column
        @SuppressWarnings("unchecked")
        Map<String, Object> featuresColumn = (Map<String, Object>) columns.get(0).get("column");
        
        assertEquals("features", featuresColumn.get("name"), "First column should be features");
        assertEquals("jsonb", featuresColumn.get("type"), "features should be jsonb type");
    }

    @Test
    void testOnboardingCompletedColumnDefinition() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changes = (List<Map<String, Object>>) changeSetDetails.get("changes");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> addColumn = (Map<String, Object>) changes.get(0).get("addColumn");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) addColumn.get("columns");
        
        // Validate onboarding_completed column
        @SuppressWarnings("unchecked")
        Map<String, Object> onboardingColumn = (Map<String, Object>) columns.get(1).get("column");
        
        assertEquals("onboarding_completed", onboardingColumn.get("name"), 
                "Second column should be onboarding_completed");
        assertEquals("boolean", onboardingColumn.get("type"), 
                "onboarding_completed should be boolean type");
        assertEquals(false, onboardingColumn.get("defaultValueBoolean"), 
                "onboarding_completed should default to false");
    }

    @Test
    void testPreconditionPreventsRerunOnExistingColumn() {
        // This test validates the logic of the precondition
        // The precondition should prevent the migration from running if the 'features' column already exists
        
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/changes/003-add-tenant-features.yaml");
        
        assertNotNull(inputStream, "Migration file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> changeSetDetails = (Map<String, Object>) changeSets.get(0).get("changeSet");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> preConditions = (List<Map<String, Object>>) changeSetDetails.get("preConditions");
        
        // Verify the precondition logic:
        // 1. onFail: MARK_RAN means if precondition fails, mark as ran (don't execute)
        // 2. not -> columnExists means "column does NOT exist"
        // Combined: If column EXISTS, precondition fails, and changeset is marked as ran (skipped)
        
        boolean hasMarkRan = preConditions.stream()
                .anyMatch(pc -> "MARK_RAN".equals(pc.get("onFail")));
        
        boolean hasNotColumnExists = preConditions.stream()
                .anyMatch(pc -> pc.containsKey("not"));
        
        assertTrue(hasMarkRan, "Should have MARK_RAN to skip on existing column");
        assertTrue(hasNotColumnExists, "Should check if column does NOT exist");
        
        // This combination ensures idempotency: the migration can be safely run multiple times
    }
}
