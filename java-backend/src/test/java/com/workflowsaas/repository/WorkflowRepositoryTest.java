package com.workflowsaas.repository;

import com.workflowsaas.entity.App;
import com.workflowsaas.entity.Workflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WorkflowRepository.
 * Tests database operations and unique constraints.
 * 
 * Note: The unique constraint was changed from (tenant_id, name, version) to (tenant_id, name)
 * to allow workflows with the same name but different versions within a tenant.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class WorkflowRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WorkflowRepository workflowRepository;

    @Autowired
    private AppRepository appRepository;

    private UUID testTenantId;
    private App testApp;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        
        // Create test app
        testApp = new App();
        testApp.setTenantId(testTenantId);
        testApp.setName("Test App");
        testApp.setApiKeyHash("test_hash");
        testApp = appRepository.save(testApp);
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testSaveWorkflow() {
        // Arrange
        Workflow workflow = createWorkflow("Test Workflow", "BPMN");

        // Act
        Workflow saved = workflowRepository.save(workflow);
        entityManager.flush();

        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    void testUniqueConstraint_TenantAndName() {
        // Arrange
        Workflow workflow1 = createWorkflow("Duplicate Name", "BPMN");
        workflowRepository.save(workflow1);
        entityManager.flush();

        // Create another workflow with same tenant and name
        Workflow workflow2 = createWorkflow("Duplicate Name", "BPMN");

        // Act & Assert - should throw exception due to unique constraint
        assertThrows(Exception.class, () -> {
            workflowRepository.save(workflow2);
            entityManager.flush();
        });
    }

    @Test
    void testUniqueConstraint_DifferentTenant() {
        // Arrange
        Workflow workflow1 = createWorkflow("Same Name", "BPMN");
        workflowRepository.save(workflow1);
        entityManager.flush();

        // Create workflow with different tenant but same name
        UUID differentTenantId = UUID.randomUUID();
        Workflow workflow2 = createWorkflow("Same Name", "BPMN");
        workflow2.setTenantId(differentTenantId);

        // Act
        Workflow saved = workflowRepository.save(workflow2);
        entityManager.flush();

        // Assert - should succeed because different tenant
        assertNotNull(saved.getId());
    }

    @Test
    void testUniqueConstraint_SameTenantDifferentNames() {
        // Arrange
        Workflow workflow1 = createWorkflow("Name One", "BPMN");
        Workflow workflow2 = createWorkflow("Name Two", "BPMN");

        // Act
        Workflow saved1 = workflowRepository.save(workflow1);
        Workflow saved2 = workflowRepository.save(workflow2);
        entityManager.flush();

        // Assert - should succeed because different names
        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    void testFindByTenantIdOrderByCreatedAtDesc() {
        // Arrange
        Workflow workflow1 = createWorkflow("Workflow 1", "BPMN");
        Workflow workflow2 = createWorkflow("Workflow 2", "BPMN");
        workflowRepository.save(workflow1);
        workflowRepository.save(workflow2);
        entityManager.flush();

        // Act
        List<Workflow> workflows = workflowRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId);

        // Assert
        assertEquals(2, workflows.size());
    }

    @Test
    void testFindByIdAndTenantId_Found() {
        // Arrange
        Workflow workflow = createWorkflow("Test Workflow", "BPMN");
        Workflow saved = workflowRepository.save(workflow);
        entityManager.flush();

        // Act
        Optional<Workflow> found = workflowRepository.findByIdAndTenantId(saved.getId(), testTenantId);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testFindByIdAndTenantId_WrongTenant() {
        // Arrange
        Workflow workflow = createWorkflow("Test Workflow", "BPMN");
        Workflow saved = workflowRepository.save(workflow);
        entityManager.flush();

        UUID wrongTenantId = UUID.randomUUID();

        // Act
        Optional<Workflow> found = workflowRepository.findByIdAndTenantId(saved.getId(), wrongTenantId);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByApiPath() {
        // Arrange
        String apiPath = "/tenant/service/workflow/v1";
        Workflow workflow = createWorkflow("Test Workflow", "BPMN");
        workflow.setApiPath(apiPath);
        workflowRepository.save(workflow);
        entityManager.flush();

        // Act
        boolean exists = workflowRepository.existsByApiPath(apiPath);

        // Assert
        assertTrue(exists);
    }

    @Test
    void testExistsByApiPath_NotFound() {
        // Act
        boolean exists = workflowRepository.existsByApiPath("/non/existent/path");

        // Assert
        assertFalse(exists);
    }

    @Test
    void testFindByAppIdOrderByCreatedAtDesc() {
        // Arrange
        Workflow workflow1 = createWorkflow("Workflow 1", "BPMN");
        workflow1.setApp(testApp);
        Workflow workflow2 = createWorkflow("Workflow 2", "BPMN");
        workflow2.setApp(testApp);
        
        workflowRepository.save(workflow1);
        workflowRepository.save(workflow2);
        entityManager.flush();

        // Act
        List<Workflow> workflows = workflowRepository.findByAppIdOrderByCreatedAtDesc(testApp.getId());

        // Assert
        assertEquals(2, workflows.size());
    }

    @Test
    void testParentWorkflowRelationship() {
        // Arrange
        Workflow parent = createWorkflow("Parent BPMN", "BPMN");
        Workflow savedParent = workflowRepository.save(parent);
        entityManager.flush();

        Workflow child = createWorkflow("Child DMN", "DMN");
        child.setParentWorkflowId(savedParent.getId());
        Workflow savedChild = workflowRepository.save(child);
        entityManager.flush();

        // Act
        Optional<Workflow> foundChild = workflowRepository.findById(savedChild.getId());

        // Assert
        assertTrue(foundChild.isPresent());
        assertEquals(savedParent.getId(), foundChild.get().getParentWorkflowId());
    }

    @Test
    void testDeleteWorkflow() {
        // Arrange
        Workflow workflow = createWorkflow("To Delete", "BPMN");
        Workflow saved = workflowRepository.save(workflow);
        entityManager.flush();
        UUID workflowId = saved.getId();

        // Act
        workflowRepository.delete(saved);
        entityManager.flush();

        // Assert
        Optional<Workflow> found = workflowRepository.findById(workflowId);
        assertFalse(found.isPresent());
    }

    @Test
    void testUpdateWorkflow() {
        // Arrange
        Workflow workflow = createWorkflow("Original Name", "BPMN");
        Workflow saved = workflowRepository.save(workflow);
        entityManager.flush();

        // Act
        saved.setName("Updated Name");
        saved.setDescription("Updated description");
        Workflow updated = workflowRepository.save(saved);
        entityManager.flush();

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
    }

    @Test
    void testApiPathIndex() {
        // Arrange
        String apiPath = "/tenant/service/test/v1";
        Workflow workflow = createWorkflow("Test", "BPMN");
        workflow.setApiPath(apiPath);
        workflowRepository.save(workflow);
        entityManager.flush();

        // Act - This should use the index
        boolean exists = workflowRepository.existsByApiPath(apiPath);

        // Assert
        assertTrue(exists);
    }

    // Helper method
    private Workflow createWorkflow(String name, String type) {
        Workflow workflow = new Workflow();
        workflow.setTenantId(testTenantId);
        workflow.setName(name);
        workflow.setType(type);
        workflow.setBpmnXml("<bpmn>test</bpmn>");
        workflow.setApp(testApp);
        return workflow;
    }
}
