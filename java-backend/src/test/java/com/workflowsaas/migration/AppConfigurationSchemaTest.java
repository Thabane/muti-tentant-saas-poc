package com.workflowsaas.migration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for app configuration database schema migration.
 * Validates that Liquibase migration 006 creates all required tables,
 * constraints, and indexes correctly.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@org.springframework.test.context.TestPropertySource(properties = {
    "spring.liquibase.enabled=true"
})
class AppConfigurationSchemaTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testAppConfigurationsTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_name = 'app_configurations'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "app_configurations table should exist");
    }

    @Test
    void testAppConfigurationsTableColumns() {
        String sql = "SELECT column_name, data_type, is_nullable " +
                     "FROM information_schema.columns " +
                     "WHERE table_name = 'app_configurations' " +
                     "ORDER BY ordinal_position";
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql);
        
        assertFalse(columns.isEmpty(), "app_configurations should have columns");
        
        // Verify key columns exist
        List<String> columnNames = columns.stream()
                .map(col -> (String) col.get("column_name"))
                .toList();
        
        assertTrue(columnNames.contains("id"), "Should have id column");
        assertTrue(columnNames.contains("tenant_id"), "Should have tenant_id column");
        assertTrue(columnNames.contains("app_id"), "Should have app_id column");
        assertTrue(columnNames.contains("source"), "Should have source column");
        assertTrue(columnNames.contains("parameters_filename"), "Should have parameters_filename column");
        assertTrue(columnNames.contains("parameters_content"), "Should have parameters_content column");
        assertTrue(columnNames.contains("model_filename"), "Should have model_filename column");
        assertTrue(columnNames.contains("model_content"), "Should have model_content column");
        assertTrue(columnNames.contains("created_at"), "Should have created_at column");
        assertTrue(columnNames.contains("updated_at"), "Should have updated_at column");
    }

    @Test
    void testEnrichmentApiConfigsTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_name = 'enrichment_api_configs'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "enrichment_api_configs table should exist");
    }

    @Test
    void testEnrichmentApiConfigsTableColumns() {
        String sql = "SELECT column_name FROM information_schema.columns " +
                     "WHERE table_name = 'enrichment_api_configs' " +
                     "ORDER BY ordinal_position";
        
        List<String> columnNames = jdbcTemplate.queryForList(sql, String.class);
        
        assertTrue(columnNames.contains("id"), "Should have id column");
        assertTrue(columnNames.contains("tenant_id"), "Should have tenant_id column");
        assertTrue(columnNames.contains("app_config_id"), "Should have app_config_id column");
        assertTrue(columnNames.contains("openapi_document"), "Should have openapi_document column");
        assertTrue(columnNames.contains("request_mappings"), "Should have request_mappings column");
        assertTrue(columnNames.contains("config_properties"), "Should have config_properties column");
        assertTrue(columnNames.contains("created_at"), "Should have created_at column");
        assertTrue(columnNames.contains("updated_at"), "Should have updated_at column");
    }

    @Test
    void testPublisherConfigsTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_name = 'publisher_configs'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "publisher_configs table should exist");
    }

    @Test
    void testPublisherConfigsTableColumns() {
        String sql = "SELECT column_name FROM information_schema.columns " +
                     "WHERE table_name = 'publisher_configs' " +
                     "ORDER BY ordinal_position";
        
        List<String> columnNames = jdbcTemplate.queryForList(sql, String.class);
        
        assertTrue(columnNames.contains("id"), "Should have id column");
        assertTrue(columnNames.contains("tenant_id"), "Should have tenant_id column");
        assertTrue(columnNames.contains("app_config_id"), "Should have app_config_id column");
        assertTrue(columnNames.contains("type"), "Should have type column");
        assertTrue(columnNames.contains("openapi_document"), "Should have openapi_document column");
        assertTrue(columnNames.contains("request_mappings"), "Should have request_mappings column");
        assertTrue(columnNames.contains("config_properties"), "Should have config_properties column");
        assertTrue(columnNames.contains("created_at"), "Should have created_at column");
        assertTrue(columnNames.contains("updated_at"), "Should have updated_at column");
    }

    @Test
    void testWarehousePublisherConfigsTableExists() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_name = 'warehouse_publisher_configs'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "warehouse_publisher_configs table should exist");
    }

    @Test
    void testWarehousePublisherConfigsTableColumns() {
        String sql = "SELECT column_name FROM information_schema.columns " +
                     "WHERE table_name = 'warehouse_publisher_configs' " +
                     "ORDER BY ordinal_position";
        
        List<String> columnNames = jdbcTemplate.queryForList(sql, String.class);
        
        assertTrue(columnNames.contains("id"), "Should have id column");
        assertTrue(columnNames.contains("tenant_id"), "Should have tenant_id column");
        assertTrue(columnNames.contains("publisher_config_id"), "Should have publisher_config_id column");
        assertTrue(columnNames.contains("avro_schema"), "Should have avro_schema column");
        assertTrue(columnNames.contains("mappings"), "Should have mappings column");
        assertTrue(columnNames.contains("created_at"), "Should have created_at column");
        assertTrue(columnNames.contains("updated_at"), "Should have updated_at column");
    }

    @Test
    void testForeignKeyConstraintsExist() {
        // Check app_configurations foreign keys
        String sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
                     "WHERE table_name = 'app_configurations' " +
                     "AND constraint_type = 'FOREIGN KEY'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "app_configurations should have 2 foreign keys (tenant_id, app_id)");
        
        // Check enrichment_api_configs foreign keys
        sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
              "WHERE table_name = 'enrichment_api_configs' " +
              "AND constraint_type = 'FOREIGN KEY'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "enrichment_api_configs should have 2 foreign keys (tenant_id, app_config_id)");
        
        // Check publisher_configs foreign keys
        sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
              "WHERE table_name = 'publisher_configs' " +
              "AND constraint_type = 'FOREIGN KEY'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "publisher_configs should have 2 foreign keys (tenant_id, app_config_id)");
        
        // Check warehouse_publisher_configs foreign keys
        sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
              "WHERE table_name = 'warehouse_publisher_configs' " +
              "AND constraint_type = 'FOREIGN KEY'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "warehouse_publisher_configs should have 2 foreign keys (tenant_id, publisher_config_id)");
    }

    @Test
    void testUniqueConstraintsExist() {
        // Check app_configurations unique constraint
        String sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
                     "WHERE table_name = 'app_configurations' " +
                     "AND constraint_type = 'UNIQUE' " +
                     "AND constraint_name = 'unique_config_per_app'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "app_configurations should have unique constraint on app_id");
        
        // Check publisher_configs unique constraint
        sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
              "WHERE table_name = 'publisher_configs' " +
              "AND constraint_type = 'UNIQUE' " +
              "AND constraint_name = 'unique_publisher_per_app_config'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "publisher_configs should have unique constraint on app_config_id");
        
        // Check warehouse_publisher_configs unique constraint
        sql = "SELECT COUNT(*) FROM information_schema.table_constraints " +
              "WHERE table_name = 'warehouse_publisher_configs' " +
              "AND constraint_type = 'UNIQUE' " +
              "AND constraint_name = 'unique_warehouse_per_publisher'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "warehouse_publisher_configs should have unique constraint on publisher_config_id");
    }

    @Test
    void testIndexesExist() {
        // Check app_configurations indexes
        String sql = "SELECT COUNT(*) FROM information_schema.indexes " +
                     "WHERE table_name = 'APP_CONFIGURATIONS' " +
                     "AND index_name IN ('IDX_APP_CONFIG_TENANT_ID', 'IDX_APP_CONFIG_APP_ID')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "app_configurations should have 2 indexes");
        
        // Check enrichment_api_configs indexes
        sql = "SELECT COUNT(*) FROM information_schema.indexes " +
              "WHERE table_name = 'ENRICHMENT_API_CONFIGS' " +
              "AND index_name IN ('IDX_ENRICHMENT_API_TENANT_ID', 'IDX_ENRICHMENT_API_CONFIG_ID')";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "enrichment_api_configs should have 2 indexes");
        
        // Check publisher_configs indexes
        sql = "SELECT COUNT(*) FROM information_schema.indexes " +
              "WHERE table_name = 'PUBLISHER_CONFIGS' " +
              "AND index_name IN ('IDX_PUBLISHER_CONFIG_TENANT_ID', 'IDX_PUBLISHER_CONFIG_APP_CONFIG_ID')";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "publisher_configs should have 2 indexes");
        
        // Check warehouse_publisher_configs indexes
        sql = "SELECT COUNT(*) FROM information_schema.indexes " +
              "WHERE table_name = 'WAREHOUSE_PUBLISHER_CONFIGS' " +
              "AND index_name IN ('IDX_WAREHOUSE_PUBLISHER_TENANT_ID', 'IDX_WAREHOUSE_PUBLISHER_CONFIG_ID')";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(2, count, "warehouse_publisher_configs should have 2 indexes");
    }

    @Test
    void testCheckConstraintsExist() {
        // Check app_configurations source check constraint
        String sql = "SELECT COUNT(*) FROM information_schema.check_constraints " +
                     "WHERE constraint_name = 'check_source_value'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "app_configurations should have check constraint on source column");
        
        // Check publisher_configs type check constraint
        sql = "SELECT COUNT(*) FROM information_schema.check_constraints " +
              "WHERE constraint_name = 'check_publisher_type_value'";
        count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertEquals(1, count, "publisher_configs should have check constraint on type column");
    }
}
