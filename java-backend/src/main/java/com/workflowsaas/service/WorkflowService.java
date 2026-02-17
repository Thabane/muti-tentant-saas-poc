package com.workflowsaas.service;

import com.workflowsaas.dto.request.CreateWorkflowRequest;
import com.workflowsaas.dto.request.TestRunRequest;
import com.workflowsaas.dto.request.UpdateWorkflowRequest;
import com.workflowsaas.dto.response.WorkflowExecutionResult;
import com.workflowsaas.dto.response.WorkflowInfo;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.entity.WorkflowExecution;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.exception.UnauthorizedException;
import com.workflowsaas.repository.WorkflowExecutionRepository;
import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.security.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for workflow management operations.
 */
@Service
public class WorkflowService {
    
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    
    public WorkflowService(WorkflowRepository workflowRepository,
                          WorkflowExecutionRepository executionRepository) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
    }
    
    @Transactional
    public Workflow createWorkflow(CreateWorkflowRequest request) {
        var tenantId = getTenantId();
        
        Workflow workflow = new Workflow();
        workflow.setTenantId(tenantId);
        workflow.setName(request.name());
        workflow.setType(request.type());
        workflow.setBpmnXml(request.bpmnXml());
        workflow.setDmnXml(request.dmnXml());
        workflow.setVersion(1);
        workflow.setStatus("draft");
        
        return workflowRepository.save(workflow);
    }
    
    @Transactional(readOnly = true)
    public List<Workflow> getAllWorkflows() {
        var tenantId = getTenantId();
        return workflowRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    @Transactional(readOnly = true)
    public Workflow getWorkflowById(UUID id) {
        var tenantId = getTenantId();
        return workflowRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
    }
    
    @Transactional
    public Workflow updateWorkflow(UUID id, UpdateWorkflowRequest request) {
        var tenantId = getTenantId();
        
        Workflow workflow = workflowRepository.findByIdAndTenantId(id, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        
        if (request.name() != null) {
            workflow.setName(request.name());
        }
        if (request.bpmnXml() != null) {
            workflow.setBpmnXml(request.bpmnXml());
        }
        if (request.dmnXml() != null) {
            workflow.setDmnXml(request.dmnXml());
        }
        
        return workflowRepository.save(workflow);
    }
    
    @Transactional
    public WorkflowExecutionResult testRun(UUID workflowId, TestRunRequest request) {
        var tenantId = getTenantId();
        
        Workflow workflow = workflowRepository.findByIdAndTenantId(workflowId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        
        WorkflowExecution execution = new WorkflowExecution();
        execution.setTenantId(tenantId);
        execution.setWorkflowId(workflowId);
        execution.setEnvironment("test");
        execution.setInputData(request.inputData());
        execution.setStatus("completed");
        
        Map<String, Object> mockOutput = new HashMap<>();
        mockOutput.put("result", "success");
        mockOutput.put("message", "Test execution completed");
        execution.setOutputData(mockOutput);
        
        execution = executionRepository.save(execution);
        
        return new WorkflowExecutionResult(
            execution.getId(),
            execution.getStatus(),
            execution.getInputData(),
            execution.getOutputData(),
            new WorkflowInfo(workflow.getId(), workflow.getName())
        );
    }
    
    private UUID getTenantId() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        return tenantId;
    }
}
