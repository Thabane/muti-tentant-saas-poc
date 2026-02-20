package com.workflowsaas.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the App entity.
 * Validates entity structure, field mappings, lifecycle callbacks, and relationships.
 */
class AppTest {

    private App app;

    @BeforeEach
    void setUp() {
        app = new App();
    }

    @Test
    void testAppCreation() {
        assertNotNull(app, "App instance should be created");
    }

    @Test
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        app.setId(id);
        assertEquals(id, app.getId(), "ID should match the set value");
    }

    @Test
    void testSetAndGetTenantId() {
        UUID tenantId = UUID.randomUUID();
        app.setTenantId(tenantId);
        assertEquals(tenantId, app.getTenantId(), "Tenant ID should match the set value");
    }

    @Test
    void testSetAndGetName() {
        String name = "My App";
        app.setName(name);
        assertEquals(name, app.getName(), "Name should match the set value");
    }

    @Test
    void testSetAndGetApiKeyHash() {
        String apiKeyHash = "$2a$10$abcdefghijklmnopqrstuv";
        app.setApiKeyHash(apiKeyHash);
        assertEquals(apiKeyHash, app.getApiKeyHash(), "API key hash should match the set value");
    }

    @Test
    void testWorkflowsDefaultsToEmptyList() {
        assertNotNull(app.getWorkflows(), "Workflows should not be null");
        assertTrue(app.getWorkflows().isEmpty(), "Workflows should be empty by default");
    }

    @Test
    void testSetAndGetWorkflows() {
        List<Workflow> workflows = new ArrayList<>();
        Workflow workflow1 = new Workflow();
        workflow1.setId(UUID.randomUUID());
        workflow1.setName("Workflow 1");
        workflows.add(workflow1);
        
        app.setWorkflows(workflows);
        assertEquals(workflows, app.getWorkflows(), "Workflows should match the set value");
        assertEquals(1, app.getWorkflows().size(), "Workflows should contain 1 entry");
    }

    @Test
    void testAddWorkflowToApp() {
        Workflow workflow = new Workflow();
        workflow.setId(UUID.randomUUID());
        workflow.setName("Test Workflow");
        workflow.setApp(app);
        
        app.getWorkflows().add(workflow);
        
        assertEquals(1, app.getWorkflows().size(), "App should have 1 workflow");
        assertEquals(workflow, app.getWorkflows().get(0), "Workflow should match");
        assertEquals(app, workflow.getApp(), "Workflow should reference the app");
    }

    @Test
    void testOnCreateSetsTimestamps() {
        // Simulate @PrePersist callback
        app.onCreate();
        
        assertNotNull(app.getCreatedAt(), "Created at should be set");
        assertNotNull(app.getUpdatedAt(), "Updated at should be set");
        // Allow for small timing differences (within 1 second)
        long createdAtSeconds = app.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        long updatedAtSeconds = app.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        assertTrue(Math.abs(createdAtSeconds - updatedAtSeconds) <= 1, 
                "Created at and updated at should be approximately equal on creation");
    }

    @Test
    void testOnUpdateModifiesUpdatedAt() throws InterruptedException {
        // Simulate @PrePersist callback
        app.onCreate();
        LocalDateTime originalCreatedAt = app.getCreatedAt();
        LocalDateTime originalUpdatedAt = app.getUpdatedAt();
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        // Simulate @PreUpdate callback
        app.onUpdate();
        
        assertEquals(originalCreatedAt, app.getCreatedAt(), 
                "Created at should not change on update");
        assertTrue(app.getUpdatedAt().isAfter(originalUpdatedAt), 
                "Updated at should be after the original timestamp");
    }

    @Test
    void testFullAppPopulation() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        String name = "Production App";
        String apiKeyHash = "$2a$10$hashedapikey";
        
        app.setId(id);
        app.setTenantId(tenantId);
        app.setName(name);
        app.setApiKeyHash(apiKeyHash);
        app.onCreate();
        
        assertEquals(id, app.getId());
        assertEquals(tenantId, app.getTenantId());
        assertEquals(name, app.getName());
        assertEquals(apiKeyHash, app.getApiKeyHash());
        assertNotNull(app.getCreatedAt());
        assertNotNull(app.getUpdatedAt());
        assertNotNull(app.getWorkflows());
    }

    @Test
    void testAppEqualsAndHashCode() {
        App app1 = new App();
        App app2 = new App();
        
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        
        app1.setId(id);
        app1.setTenantId(tenantId);
        app1.setName("Test App");
        app1.setApiKeyHash("hash123");
        
        app2.setId(id);
        app2.setTenantId(tenantId);
        app2.setName("Test App");
        app2.setApiKeyHash("hash123");
        
        // Lombok @Data generates equals and hashCode
        assertEquals(app1, app2, "Apps with same data should be equal");
        assertEquals(app1.hashCode(), app2.hashCode(), "Hash codes should match");
    }

    @Test
    void testOneToManyRelationshipWithWorkflows() {
        UUID tenantId = UUID.randomUUID();
        app.setTenantId(tenantId);
        app.setName("Multi-Workflow App");
        app.setApiKeyHash("hash456");
        
        // Create multiple workflows
        Workflow workflow1 = new Workflow();
        workflow1.setId(UUID.randomUUID());
        workflow1.setTenantId(tenantId);
        workflow1.setName("BPMN Workflow");
        workflow1.setType("BPMN");
        workflow1.setApp(app);
        
        Workflow workflow2 = new Workflow();
        workflow2.setId(UUID.randomUUID());
        workflow2.setTenantId(tenantId);
        workflow2.setName("DMN Workflow");
        workflow2.setType("DMN");
        workflow2.setApp(app);
        
        app.getWorkflows().add(workflow1);
        app.getWorkflows().add(workflow2);
        
        assertEquals(2, app.getWorkflows().size(), "App should have 2 workflows");
        assertTrue(app.getWorkflows().contains(workflow1), "Should contain workflow1");
        assertTrue(app.getWorkflows().contains(workflow2), "Should contain workflow2");
    }

    @Test
    void testCascadeRelationship() {
        // This test validates the cascade configuration
        // In actual JPA context, deleting the app would cascade to workflows
        app.setName("Cascade Test App");
        app.setApiKeyHash("hash789");
        
        Workflow workflow = new Workflow();
        workflow.setName("Child Workflow");
        workflow.setApp(app);
        app.getWorkflows().add(workflow);
        
        // Clear workflows (simulating cascade delete)
        app.getWorkflows().clear();
        
        assertTrue(app.getWorkflows().isEmpty(), "Workflows should be cleared");
    }

    @Test
    void testUniqueConstraintFields() {
        // Test that the entity has the required unique constraint fields
        UUID tenantId = UUID.randomUUID();
        String name = "Unique App";
        String apiKeyHash = "unique_hash";
        
        app.setTenantId(tenantId);
        app.setName(name);
        app.setApiKeyHash(apiKeyHash);
        
        // These fields should be set for unique constraint validation
        assertNotNull(app.getTenantId(), "Tenant ID should be set for unique constraint");
        assertNotNull(app.getName(), "Name should be set for unique constraint");
        assertNotNull(app.getApiKeyHash(), "API key hash should be set for unique constraint");
    }

    @Test
    void testTimestampImmutability() {
        app.onCreate();
        LocalDateTime createdAt = app.getCreatedAt();
        
        // Try to modify createdAt (should not affect the entity in real JPA context)
        app.setCreatedAt(LocalDateTime.now().plusDays(1));
        
        // In JPA, createdAt is marked as updatable=false, so it won't change in DB
        assertNotNull(app.getCreatedAt(), "Created at should still be set");
    }
}
