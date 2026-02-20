package com.workflowsaas.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Workflow entity.
 * Tests entity validation, lifecycle callbacks, and business rules.
 */
class WorkflowTest {

    @Test
    void testWorkflowCreation() {
        // Arrange & Act
        Workflow workflow = new Workflow();
        workflow.setId(UUID.randomUUID());
        workflow.setTenantId(UUID.randomUUID());
        workflow.setName("Test Workflow");
        workflow.setType("BPMN");
        workflow.setBpmnXml("<bpmn>test</bpmn>");

        // Assert
        assertNotNull(workflow.getId());
        assertNotNull(workflow.getTenantId());
        assertEquals("Test Workflow", workflow.getName());
        assertEquals("BPMN", workflow.getType());
        assertEquals("<bpmn>test</bpmn>", workflow.getBpmnXml());
    }

    @Test
    void testOnCreateCallback() {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setTenantId(UUID.randomUUID());
        workflow.setName("Test Workflow");
        workflow.setType("BPMN");

        // Act
        workflow.onCreate();

        // Assert
        assertNotNull(workflow.getCreatedAt());
        assertNotNull(workflow.getUpdatedAt());
        assertEquals(workflow.getCreatedAt(), workflow.getUpdatedAt());
    }

    @Test
    void testOnUpdateCallback() throws InterruptedException {
        // Arrange
        Workflow workflow = new Workflow();
        workflow.setTenantId(UUID.randomUUID());
        workflow.setName("Test Workflow");
        workflow.setType("BPMN");
        workflow.onCreate();
        
        LocalDateTime originalUpdatedAt = workflow.getUpdatedAt();
        Thread.sleep(10); // Ensure time difference

        // Act
        workflow.onUpdate();

        // Assert
        assertNotNull(workflow.getUpdatedAt());
        assertTrue(workflow.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals(workflow.getCreatedAt(), originalUpdatedAt); // createdAt should not change
    }

    @Test
    void testDefaultVersionValue() {
        // Arrange & Act
        Workflow workflow = new Workflow();

        // Assert
        assertEquals(1, workflow.getVersion());
    }

    @Test
    void testDefaultStatusValue() {
        // Arrange & Act
        Workflow workflow = new Workflow();

        // Assert
        assertEquals("draft", workflow.getStatus());
    }

    @Test
    void testAppRelationship() {
        // Arrange
        App app = new App();
        app.setId(UUID.randomUUID());
        app.setName("Test App");

        Workflow workflow = new Workflow();
        workflow.setApp(app);

        // Assert
        assertNotNull(workflow.getApp());
        assertEquals(app.getId(), workflow.getApp().getId());
    }

    @Test
    void testParentWorkflowId() {
        // Arrange
        UUID parentId = UUID.randomUUID();
        Workflow workflow = new Workflow();
        workflow.setParentWorkflowId(parentId);

        // Assert
        assertEquals(parentId, workflow.getParentWorkflowId());
    }

    @Test
    void testApiPath() {
        // Arrange
        String apiPath = "/tenant-id/sub-service/workflow-name/v1";
        Workflow workflow = new Workflow();
        workflow.setApiPath(apiPath);

        // Assert
        assertEquals(apiPath, workflow.getApiPath());
    }

    @Test
    void testSubService() {
        // Arrange
        String subService = "financial";
        Workflow workflow = new Workflow();
        workflow.setSubService(subService);

        // Assert
        assertEquals(subService, workflow.getSubService());
    }

    @Test
    void testWorkflowTypes() {
        // Test BPMN type
        Workflow bpmn = new Workflow();
        bpmn.setType("BPMN");
        assertEquals("BPMN", bpmn.getType());

        // Test DMN type
        Workflow dmn = new Workflow();
        dmn.setType("DMN");
        assertEquals("DMN", dmn.getType());
    }

    @Test
    void testDescriptionOptional() {
        // Arrange & Act
        Workflow workflow = new Workflow();
        workflow.setName("Test");
        workflow.setType("BPMN");

        // Assert - description can be null
        assertNull(workflow.getDescription());
    }

    @Test
    void testBpmnXmlStorage() {
        // Arrange
        String largeBpmnXml = "<bpmn>" + "x".repeat(10000) + "</bpmn>";
        Workflow workflow = new Workflow();
        workflow.setBpmnXml(largeBpmnXml);

        // Assert
        assertEquals(largeBpmnXml, workflow.getBpmnXml());
        assertTrue(workflow.getBpmnXml().length() > 1000);
    }
}
