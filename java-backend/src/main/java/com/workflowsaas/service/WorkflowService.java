package com.workflowsaas.service;

import com.workflowsaas.dto.request.CreateWorkflowRequest;
import com.workflowsaas.dto.request.TestRunRequest;
import com.workflowsaas.dto.request.UpdateWorkflowRequest;
import com.workflowsaas.dto.response.WorkflowExecutionResult;
import com.workflowsaas.dto.response.WorkflowInfo;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.entity.WorkflowExecution;
import com.workflowsaas.exception.ResourceNotFoundException;
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
    private final ApiPathGenerator apiPathGenerator;
    
    public WorkflowService(WorkflowRepository workflowRepository,
                          WorkflowExecutionRepository executionRepository,
                          ApiPathGenerator apiPathGenerator) {
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.apiPathGenerator = apiPathGenerator;
    }
    
    @Transactional
    public Workflow createWorkflow(CreateWorkflowRequest request) {
        var tenantId = getTenantId();
        
        // Validate app association
        if (request.appId() == null) {
            throw new IllegalArgumentException("Resource must be associated with an app");
        }
        
        // Validate parent-child relationships for DMN
        if (request.parentWorkflowId() != null) {
            Workflow parentWorkflow = workflowRepository.findById(request.parentWorkflowId())
                .orElseThrow(() -> new IllegalArgumentException("Parent workflow must be a valid BPMN resource"));
            
            if (!"BPMN".equals(parentWorkflow.getType())) {
                throw new IllegalArgumentException("Parent workflow must be a valid BPMN resource");
            }
            
            // Prevent cross-app reference
            if (!parentWorkflow.getApp().getId().equals(request.appId())) {
                throw new IllegalArgumentException("DMN must reference BPMN within the same app");
            }
        }
        
        Workflow workflow = new Workflow();
        workflow.setTenantId(tenantId);
        workflow.setName(request.name());
        workflow.setType(request.type());
        // Both BPMN and DMN content are stored in bpmn_xml column
        String xmlContent = request.bpmnXml() != null ? request.bpmnXml() : request.dmnXml();
        workflow.setBpmnXml(xmlContent);
        workflow.setVersion(1);
        workflow.setStatus("draft");
        workflow.setParentWorkflowId(request.parentWorkflowId());
        workflow.setSubService(request.subService() != null ? request.subService() : "default");
        
        // Generate API path for BPMN services
        if ("BPMN".equals(request.type())) {
            String apiPath = apiPathGenerator.generateApiPath(
                tenantId,
                workflow.getSubService(),
                request.name(),
                workflow.getVersion()
            );
            workflow.setApiPath(apiPath);
        }
        
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
        // Both BPMN and DMN content are stored in bpmn_xml column
        if (request.bpmnXml() != null) {
            workflow.setBpmnXml(request.bpmnXml());
        } else if (request.dmnXml() != null) {
            workflow.setBpmnXml(request.dmnXml());
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
            new WorkflowInfo(workflow.getId(), workflow.getName(), workflow.getApiPath())
        );
    }
    
    private UUID getTenantId() {
        var tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            // TODO: When security is re-enabled, this should throw UnauthorizedException
            // For now, use default tenant ID since security is disabled
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }
        return tenantId;
    }
}
