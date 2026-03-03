package com.workflowsaas.migration;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify Liquibase migrations can be applied successfully.
 * This test validates that the migration files are syntactically correct
 * and can be executed against a test database.
 */
@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration"
})
@ActiveProfiles("test")
class LiquibaseMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testLiquibaseMigrationsApplySuccessfully() throws Exception {
        // This test verifies that all Liquibase migrations can be applied
        // The migrations are automatically applied by Spring Boot on startup
        // If we reach this point, it means migrations were successful
        
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            Liquibase liquibase = new Liquibase(
                    "db/changelog/db.changelog-master.yaml",
                    new ClassLoaderResourceAccessor(),
                    database
            );
            
            // Verify that migrations have been applied
            assertNotNull(liquibase, "Liquibase should be initialized");
            
            // Check that the changelog table exists (created by Liquibase)
            assertTrue(connection.getMetaData().getTables(null, null, "DATABASECHANGELOG", null).next(),
                    "Liquibase changelog table should exist");
        }
    }

    @Test
    void testAppConfigurationTablesExistAfterMigration() throws Exception {
        // Verify that the app configuration tables were created by the migration
        try (Connection connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();
            
            // Check app_configurations table
            assertTrue(metaData.getTables(null, null, "APP_CONFIGURATIONS", null).next(),
                    "app_configurations table should exist after migration");
            
            // Check enrichment_api_configs table
            assertTrue(metaData.getTables(null, null, "ENRICHMENT_API_CONFIGS", null).next(),
                    "enrichment_api_configs table should exist after migration");
            
            // Check publisher_configs table
            assertTrue(metaData.getTables(null, null, "PUBLISHER_CONFIGS", null).next(),
                    "publisher_configs table should exist after migration");
            
            // Check warehouse_publisher_configs table
            assertTrue(metaData.getTables(null, null, "WAREHOUSE_PUBLISHER_CONFIGS", null).next(),
                    "warehouse_publisher_configs table should exist after migration");
        }
    }
}
