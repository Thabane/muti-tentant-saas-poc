package com.workflowsaas.migration;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for Liquibase changelog master file.
 * Validates that the master changelog file is correctly structured
 * and references all migration files that exist in the project.
 */
class LiquibaseChangelogMasterTest {

    @Test
    void testChangelogMasterFileExists() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file db.changelog-master.yaml should exist");
    }

    @Test
    void testChangelogMasterFileStructure() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        assertNotNull(data, "YAML file should be parseable");
        assertTrue(data.containsKey("databaseChangeLog"), "Should have databaseChangeLog root element");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        assertNotNull(changeSets, "Should have changeSets");
        assertFalse(changeSets.isEmpty(), "Should have at least one changeSet");
    }

    @Test
    void testAllReferencedMigrationFilesExist() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        for (Map<String, Object> changeSet : changeSets) {
            assertTrue(changeSet.containsKey("include"), "Each entry should be an include");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> include = (Map<String, Object>) changeSet.get("include");
            
            assertTrue(include.containsKey("file"), "Include should have file attribute");
            
            String filePath = (String) include.get("file");
            assertNotNull(filePath, "File path should not be null");
            assertFalse(filePath.isEmpty(), "File path should not be empty");
            
            // Verify the referenced file exists
            InputStream migrationFile = getClass().getClassLoader()
                    .getResourceAsStream(filePath);
            
            assertNotNull(migrationFile, 
                    "Referenced migration file should exist: " + filePath);
        }
    }

    @Test
    void testMigrationFileOrder() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        // Verify we have the expected migrations in order
        assertTrue(changeSets.size() >= 8, "Should have at least 8 migration files");
        
        // Check first migration
        @SuppressWarnings("unchecked")
        Map<String, Object> firstInclude = (Map<String, Object>) changeSets.get(0).get("include");
        assertEquals("db/changelog/changes/000-create-initial-schema.yaml", 
                firstInclude.get("file"), 
                "First migration should be initial schema");
        
        // Check last migration (007-add-tenant-configuration-columns.yaml)
        @SuppressWarnings("unchecked")
        Map<String, Object> lastInclude = (Map<String, Object>) changeSets.get(changeSets.size() - 1).get("include");
        assertEquals("db/changelog/changes/007-add-tenant-configuration-columns.yaml", 
                lastInclude.get("file"), 
                "Last migration should be 007-add-tenant-configuration-columns.yaml");
    }

    @Test
    void testNoMissingMigrationFiles() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        // Verify specific migrations exist
        List<String> expectedMigrations = List.of(
                "db/changelog/changes/000-create-initial-schema.yaml",
                "db/changelog/changes/001-create-apps-table.yaml",
                "db/changelog/changes/002-update-workflows-table.yaml",
                "db/changelog/changes/003-add-tenant-features.yaml",
                "db/changelog/changes/004-insert-default-tenant.yaml",
                "db/changelog/changes/001-add-deployment-timestamps.yaml",
                "db/changelog/changes/006-create-app-configuration-tables.yaml",
                "db/changelog/changes/007-add-tenant-configuration-columns.yaml"
        );
        
        for (String expectedMigration : expectedMigrations) {
            boolean found = changeSets.stream()
                    .anyMatch(changeSet -> {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> include = (Map<String, Object>) changeSet.get("include");
                        return expectedMigration.equals(include.get("file"));
                    });
            
            assertTrue(found, "Expected migration should be present: " + expectedMigration);
        }
    }

    @Test
    void testTenantConfigurationColumnsMigrationIsIncluded() {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("db/changelog/db.changelog-master.yaml");
        
        assertNotNull(inputStream, "Changelog master file should exist");
        
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(inputStream);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> changeSets = (List<Map<String, Object>>) data.get("databaseChangeLog");
        
        // Verify the corrected filename is present
        boolean found = changeSets.stream()
                .anyMatch(changeSet -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> include = (Map<String, Object>) changeSet.get("include");
                    return "db/changelog/changes/007-add-tenant-configuration-columns.yaml"
                            .equals(include.get("file"));
                });
        
        assertTrue(found, 
                "Migration 007-add-tenant-configuration-columns.yaml should be included in master changelog");
    }
}
