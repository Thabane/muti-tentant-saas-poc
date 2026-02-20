package com.workflowsaas.service;

import com.workflowsaas.dto.camunda.CamundaDeployment;
import com.workflowsaas.dto.camunda.ProcessInstance;
import com.workflowsaas.dto.request.DeploymentRequest;
import com.workflowsaas.dto.request.ExecuteRequest;
import com.workflowsaas.dto.request.PromoteRequest;
import com.workflowsaas.entity.Deployment;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.entity.WorkflowExecution;
import com.workflowsaas.exception.BadRequestException;
import com.workflowsaas.exception.ResourceNotFoundException;
import com.workflowsaas.exception.UnauthorizedException;
import com.workflowsaas.repository.DeploymentRepository;
import com.workflowsaas.repository.WorkflowExecutionRepository;
import com.workflowsaas.repository.WorkflowRepository;
import com.workflowsaas.security.TenantContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for deployment management operations.
 */
@Service
public class DeploymentService {
    
    private final DeploymentRepository deploymentRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final CamundaService camundaService;
    
    private static final Map<String, Integer> ENV_ORDER = Map.of(
        "test", 1,
        "non-prod", 2,
        "production", 3
    );
    
    public DeploymentService(DeploymentRepository deploymentRepository,
                            WorkflowRepository workflowRepository,
                            WorkflowExecutionRepository executionRepository,
                            CamundaService camundaService) {
        this.deploymentRepository = deploymentRepository;
        this.workflowRepository = workflowRepository;
        this.executionRepository = executionRepository;
        this.camundaService = camundaService;
    }
    
    @Transactional
    public Deployment deploy(DeploymentRequest request) {
        var tenantId = getTenantId();
        
        Workflow workflow = workflowRepository.findByIdAndTenantId(request.workflowId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        
        Deployment deployment = new Deployment();
        deployment.setTenantId(tenantId);
        deployment.setWorkflowId(workflow.getId());
        deployment.setEnvironment(request.environment());
        deployment.setRolloutPercentage(request.rolloutPercentage());
        deployment.setStatus("active");
        
        String deploymentKey = String.format("%s-%s-%s", 
            workflow.getName(), request.environment(), UUID.randomUUID().toString().substring(0, 8));
        deployment.setDeploymentKey(deploymentKey);
        
        if (!"test".equals(request.environment()) && workflow.getBpmnXml() != null) {
            CamundaDeployment camundaDep = camundaService.deployBpmn(
                workflow.getBpmnXml(), 
                deploymentKey, 
                tenantId
            );
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("camundaDeploymentId", camundaDep.id());
            deployment.setMetadata(metadata);
        }
        
        return deploymentRepository.save(deployment);
    }
    
    @Transactional(readOnly = true)
    public List<Deployment> getDeployments(String environment) {
        var tenantId = getTenantId();
        
        if (environment != null) {
            return deploymentRepository.findByTenantIdAndEnvironmentOrderByDeployedAtDesc(tenantId, environment);
        }
        return deploymentRepository.findByTenantIdOrderByDeployedAtDesc(tenantId);
    }
    
    @Transactional
    public Deployment promote(UUID deploymentId, PromoteRequest request) {
        var tenantId = getTenantId();
        
        Deployment sourceDeployment = deploymentRepository.findByIdAndTenantId(deploymentId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Deployment not found"));
        
        int sourceOrder = ENV_ORDER.getOrDefault(sourceDeployment.getEnvironment(), 0);
        int targetOrder = ENV_ORDER.getOrDefault(request.targetEnvironment(), 0);
        
        if (targetOrder <= sourceOrder) {
            throw new BadRequestException("Can only promote to higher environments");
        }
        
        sourceDeployment.setPromotedAt(LocalDateTime.now());
        deploymentRepository.save(sourceDeployment);
        
        DeploymentRequest newDeploymentRequest = new DeploymentRequest(
            sourceDeployment.getWorkflowId(),
            request.targetEnvironment(),
            request.rolloutPercentage()
        );
        
        return deploy(newDeploymentRequest);
    }
    
    @Transactional
    public Deployment updateRollout(UUID deploymentId, int percentage) {
        var tenantId = getTenantId();
        
        if (percentage < 0 || percentage > 100) {
            throw new BadRequestException("Rollout percentage must be between 0 and 100");
        }
        
        Deployment deployment = deploymentRepository.findByIdAndTenantId(deploymentId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Deployment not found"));
        
        deployment.setRolloutPercentage(percentage);
        return deploymentRepository.save(deployment);
    }
    
    @Transactional
    public ProcessInstance executeWorkflow(UUID deploymentId, ExecuteRequest request) {
        var tenantId = getTenantId();
        
        Deployment deployment = deploymentRepository.findByIdAndTenantId(deploymentId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Deployment not found"));
        
        Workflow workflow = workflowRepository.findByIdAndTenantId(deployment.getWorkflowId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
        
        String processKey = camundaService.extractProcessKey(workflow.getBpmnXml());
        
        ProcessInstance instance = camundaService.startProcessInstance(
            processKey, 
            request.inputData(), 
            tenantId
        );
        
        WorkflowExecution execution = new WorkflowExecution();
        execution.setTenantId(tenantId);
        execution.setWorkflowId(workflow.getId());
        execution.setEnvironment(deployment.getEnvironment());
        execution.setInstanceKey(instance.id());
        execution.setInputData(request.inputData());
        execution.setStatus("running");
        
        executionRepository.save(execution);
        
        return instance;
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
