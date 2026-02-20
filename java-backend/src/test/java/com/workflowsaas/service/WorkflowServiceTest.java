package com.workflowsaas.service;

import com.workflowsaas.dto.request.CreateWorkflowRequest;
import com.workflowsaas.dto.request.TestRunRequest;
import com.workflowsaas.dto.request.UpdateWorkflowRequest;
import com.workflowsaas.dto.response.WorkflowExecutionResult;
import com.workflowsaas.entity.App;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.entity.WorkflowExecution;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.repository.WorkflowExecutionRepository;
import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.security.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkflowService.
 * Tests workflow management operations with mocked dependencies.
 * 
 * Note: These tests reflect the current state where security is disabled
 * and the service uses a default tenant ID when no tenant context is available.
 */
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private WorkflowExecutionRepository executionRepository;

    @Mock
    private ApiPathGenerator apiPathGenerator;

    @InjectMocks
    private WorkflowService workflowService;

    private UUID testTenantId;
    private UUID defaultTenantId;
    private UUID testAppId;
    private UUID testWorkflowId;
    private App testApp;
    private Workflow testWorkflow;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        defaultTenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        testAppId = UUID.randomUUID();
        testWorkflowId = UUID.randomUUID();
        now = LocalDateTime.now();
        
        testApp = new App();
        testApp.setId(testAppId);
        testApp.setTenantId(testTenantId);
        testApp.setName("Test App");
        
        testWorkflow = new Workflow();
        testWorkflow.setId(testWorkflowId);
        testWorkflow.setTenantId(testTenantId);
        testWorkflow.setName("Test Workflow");
        testWorkflow.setType("BPMN");
        testWorkflow.setBpmnXml("<bpmn>test</bpmn>");
        testWorkflow.setVersion(1);
        testWorkflow.setStatus("draft");
        testWorkflow.setApp(testApp);
        testWorkflow.setSubService("default");
        testWorkflow.setApiPath("/tenant/default/test-workflow/v1");
        testWorkflow.setCreatedAt(now);
        testWorkflow.setUpdatedAt(now);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testCreateWorkflow_WithTenantContext_BPMN() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "New Workflow",
            "BPMN",
            "<bpmn>content</bpmn>",
            null,
            testAppId,
            null,
            "default"
        );
        
        String generatedPath = "/" + testTenantId + "/default/new-workflow/v1";
        when(apiPathGenerator.generateApiPath(testTenantId, "default", "New Workflow", 1))
            .thenReturn(generatedPath);
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> {
            Workflow w = invocation.getArgument(0);
            w.setId(testWorkflowId);
            return w;
        });

        // Act
        Workflow result = workflowService.createWorkflow(request);

        // Assert
        assertNotNull(result);
        assertEquals("New Workflow", result.getName());
        assertEquals("BPMN", result.getType());
        assertEquals(testTenantId, result.getTenantId());
        assertEquals(generatedPath, result.getApiPath());
        assertEquals("default", result.getSubService());
        
        verify(apiPathGenerator).generateApiPath(testTenantId, "default", "New Workflow", 1);
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    void testCreateWorkflow_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear(); // No tenant context
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "New Workflow",
            "BPMN",
            "<bpmn>content</bpmn>",
            null,
            testAppId,
            null,
            "default"
        );
        
        String generatedPath = "/" + defaultTenantId + "/default/new-workflow/v1";
        when(apiPathGenerator.generateApiPath(defaultTenantId, "default", "New Workflow", 1))
            .thenReturn(generatedPath);
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> {
            Workflow w = invocation.getArgument(0);
            w.setId(testWorkflowId);
            return w;
        });

        // Act
        Workflow result = workflowService.createWorkflow(request);

        // Assert
        assertNotNull(result);
        assertEquals(defaultTenantId, result.getTenantId());
        
        verify(apiPathGenerator).generateApiPath(defaultTenantId, "default", "New Workflow", 1);
        verify(workflowRepository).save(argThat(w -> 
            w.getTenantId().equals(defaultTenantId)
        ));
    }

    @Test
    void testCreateWorkflow_DMN_WithParent() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UUID parentWorkflowId = UUID.randomUUID();
        
        Workflow parentWorkflow = new Workflow();
        parentWorkflow.setId(parentWorkflowId);
        parentWorkflow.setType("BPMN");
        parentWorkflow.setApp(testApp);
        
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "DMN Decision",
            "DMN",
            null,
            "<dmn>content</dmn>",
            testAppId,
            parentWorkflowId,
            "default"
        );
        
        when(workflowRepository.findById(parentWorkflowId)).thenReturn(Optional.of(parentWorkflow));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Workflow result = workflowService.createWorkflow(request);

        // Assert
        assertNotNull(result);
        assertEquals("DMN", result.getType());
        assertEquals(parentWorkflowId, result.getParentWorkflowId());
        assertNull(result.getApiPath()); // DMN doesn't get API path
        
        verify(workflowRepository).findById(parentWorkflowId);
        verify(workflowRepository).save(any(Workflow.class));
        verify(apiPathGenerator, never()).generateApiPath(any(), any(), any(), anyInt());
    }

    @Test
    void testCreateWorkflow_NoAppId_ThrowsException() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "New Workflow",
            "BPMN",
            "<bpmn>content</bpmn>",
            null,
            null, // No app ID
            null,
            "default"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workflowService.createWorkflow(request)
        );
        
        assertEquals("Resource must be associated with an app", exception.getMessage());
        verify(workflowRepository, never()).save(any());
    }

    @Test
    void testCreateWorkflow_InvalidParent_ThrowsException() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UUID invalidParentId = UUID.randomUUID();
        
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "DMN Decision",
            "DMN",
            null,
            "<dmn>content</dmn>",
            testAppId,
            invalidParentId,
            "default"
        );
        
        when(workflowRepository.findById(invalidParentId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workflowService.createWorkflow(request)
        );
        
        assertTrue(exception.getMessage().contains("Parent workflow must be a valid BPMN resource"));
    }

    @Test
    void testCreateWorkflow_ParentNotBPMN_ThrowsException() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UUID parentId = UUID.randomUUID();
        
        Workflow parentWorkflow = new Workflow();
        parentWorkflow.setId(parentId);
        parentWorkflow.setType("DMN"); // Parent is DMN, not BPMN
        parentWorkflow.setApp(testApp);
        
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "DMN Decision",
            "DMN",
            null,
            "<dmn>content</dmn>",
            testAppId,
            parentId,
            "default"
        );
        
        when(workflowRepository.findById(parentId)).thenReturn(Optional.of(parentWorkflow));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workflowService.createWorkflow(request)
        );
        
        assertEquals("Parent workflow must be a valid BPMN resource", exception.getMessage());
    }

    @Test
    void testCreateWorkflow_CrossAppReference_ThrowsException() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UUID parentId = UUID.randomUUID();
        UUID differentAppId = UUID.randomUUID();
        
        App differentApp = new App();
        differentApp.setId(differentAppId);
        
        Workflow parentWorkflow = new Workflow();
        parentWorkflow.setId(parentId);
        parentWorkflow.setType("BPMN");
        parentWorkflow.setApp(differentApp); // Different app
        
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "DMN Decision",
            "DMN",
            null,
            "<dmn>content</dmn>",
            testAppId, // Different from parent's app
            parentId,
            "default"
        );
        
        when(workflowRepository.findById(parentId)).thenReturn(Optional.of(parentWorkflow));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> workflowService.createWorkflow(request)
        );
        
        assertEquals("DMN must reference BPMN within the same app", exception.getMessage());
    }

    @Test
    void testGetAllWorkflows_WithTenantContext() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        List<Workflow> workflows = Arrays.asList(testWorkflow);
        when(workflowRepository.findByTenantIdOrderByCreatedAtDesc(testTenantId)).thenReturn(workflows);

        // Act
        List<Workflow> result = workflowService.getAllWorkflows();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testWorkflowId, result.get(0).getId());
        verify(workflowRepository).findByTenantIdOrderByCreatedAtDesc(testTenantId);
    }

    @Test
    void testGetAllWorkflows_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        List<Workflow> workflows = Arrays.asList(testWorkflow);
        when(workflowRepository.findByTenantIdOrderByCreatedAtDesc(defaultTenantId)).thenReturn(workflows);

        // Act
        List<Workflow> result = workflowService.getAllWorkflows();

        // Assert
        assertNotNull(result);
        verify(workflowRepository).findByTenantIdOrderByCreatedAtDesc(defaultTenantId);
    }

    @Test
    void testGetWorkflowById_WithTenantContext_Found() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, testTenantId))
            .thenReturn(Optional.of(testWorkflow));

        // Act
        Workflow result = workflowService.getWorkflowById(testWorkflowId);

        // Assert
        assertNotNull(result);
        assertEquals(testWorkflowId, result.getId());
        verify(workflowRepository).findByIdAndTenantId(testWorkflowId, testTenantId);
    }

    @Test
    void testGetWorkflowById_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, defaultTenantId))
            .thenReturn(Optional.of(testWorkflow));

        // Act
        Workflow result = workflowService.getWorkflowById(testWorkflowId);

        // Assert
        assertNotNull(result);
        verify(workflowRepository).findByIdAndTenantId(testWorkflowId, defaultTenantId);
    }

    @Test
    void testGetWorkflowById_NotFound() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, testTenantId))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> workflowService.getWorkflowById(testWorkflowId)
        );
        
        assertEquals("Workflow not found", exception.getMessage());
    }

    @Test
    void testUpdateWorkflow_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        UpdateWorkflowRequest request = new UpdateWorkflowRequest(
            "Updated Name",
            "<bpmn>updated</bpmn>",
            null
        );
        
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, testTenantId))
            .thenReturn(Optional.of(testWorkflow));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Workflow result = workflowService.updateWorkflow(testWorkflowId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("<bpmn>updated</bpmn>", result.getBpmnXml());
        verify(workflowRepository).save(testWorkflow);
    }

    @Test
    void testUpdateWorkflow_DMN_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        testWorkflow.setType("DMN");
        
        UpdateWorkflowRequest request = new UpdateWorkflowRequest(
            "Updated DMN",
            null,
            "<dmn>updated</dmn>"
        );
        
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, testTenantId))
            .thenReturn(Optional.of(testWorkflow));
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Workflow result = workflowService.updateWorkflow(testWorkflowId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Updated DMN", result.getName());
        assertEquals("<dmn>updated</dmn>", result.getBpmnXml());
    }

    @Test
    void testTestRun_WithTenantContext_Success() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("key", "value");
        
        TestRunRequest request = new TestRunRequest(inputData);
        
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, testTenantId))
            .thenReturn(Optional.of(testWorkflow));
        when(executionRepository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> {
            WorkflowExecution execution = invocation.getArgument(0);
            execution.setId(UUID.randomUUID());
            return execution;
        });

        // Act
        WorkflowExecutionResult result = workflowService.testRun(testWorkflowId, request);

        // Assert
        assertNotNull(result);
        assertEquals("completed", result.status());
        assertNotNull(result.workflow());
        assertEquals(testWorkflowId, result.workflow().id());
        assertEquals("Test Workflow", result.workflow().name());
        
        verify(workflowRepository).findByIdAndTenantId(testWorkflowId, testTenantId);
        verify(executionRepository).save(any(WorkflowExecution.class));
    }

    @Test
    void testTestRun_WithoutTenantContext_UsesDefaultTenant() {
        // Arrange
        TenantContextHolder.clear();
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("key", "value");
        
        TestRunRequest request = new TestRunRequest(inputData);
        
        when(workflowRepository.findByIdAndTenantId(testWorkflowId, defaultTenantId))
            .thenReturn(Optional.of(testWorkflow));
        when(executionRepository.save(any(WorkflowExecution.class))).thenAnswer(invocation -> {
            WorkflowExecution execution = invocation.getArgument(0);
            execution.setId(UUID.randomUUID());
            return execution;
        });

        // Act
        WorkflowExecutionResult result = workflowService.testRun(testWorkflowId, request);

        // Assert
        assertNotNull(result);
        verify(workflowRepository).findByIdAndTenantId(testWorkflowId, defaultTenantId);
        verify(executionRepository).save(argThat(execution -> 
            execution.getTenantId().equals(defaultTenantId)
        ));
    }

    @Test
    void testCreateWorkflow_DefaultSubService() {
        // Arrange
        TenantContextHolder.setTenantId(testTenantId);
        CreateWorkflowRequest request = new CreateWorkflowRequest(
            "New Workflow",
            "BPMN",
            "<bpmn>content</bpmn>",
            null,
            testAppId,
            null,
            null // No sub-service specified
        );
        
        String generatedPath = "/" + testTenantId + "/default/new-workflow/v1";
        when(apiPathGenerator.generateApiPath(testTenantId, "default", "New Workflow", 1))
            .thenReturn(generatedPath);
        when(workflowRepository.save(any(Workflow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Workflow result = workflowService.createWorkflow(request);

        // Assert
        assertEquals("default", result.getSubService());
        verify(apiPathGenerator).generateApiPath(testTenantId, "default", "New Workflow", 1);
    }
}
