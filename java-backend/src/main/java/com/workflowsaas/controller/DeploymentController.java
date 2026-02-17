package com.workflowsaas.controller;

import com.workflowsaas.dto.camunda.ProcessInstance;
import com.workflowsaas.dto.request.DeploymentRequest;
import com.workflowsaas.dto.request.ExecuteRequest;
import com.workflowsaas.dto.request.PromoteRequest;
import com.workflowsaas.dto.request.RolloutRequest;
import com.workflowsaas.entity.Deployment;
import com.workflowsaas.service.DeploymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for deployment management endpoints.
 */
@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {
    
    private final DeploymentService deploymentService;
    
    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }
    
    @PostMapping
    public ResponseEntity<Deployment> createDeployment(@RequestBody DeploymentRequest request) {
        Deployment deployment = deploymentService.deploy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deployment);
    }
    
    @GetMapping
    public ResponseEntity<List<Deployment>> getDeployments(
            @RequestParam(required = false) String environment) {
        List<Deployment> deployments = deploymentService.getDeployments(environment);
        return ResponseEntity.ok(deployments);
    }
    
    @PostMapping("/{id}/promote")
    public ResponseEntity<Deployment> promoteDeployment(@PathVariable UUID id, 
                                                       @RequestBody PromoteRequest request) {
        Deployment deployment = deploymentService.promote(id, request);
        return ResponseEntity.ok(deployment);
    }
    
    @PostMapping("/{id}/rollout")
    public ResponseEntity<Deployment> updateRollout(@PathVariable UUID id, 
                                                    @RequestBody RolloutRequest request) {
        Deployment deployment = deploymentService.updateRollout(id, request.percentage());
        return ResponseEntity.ok(deployment);
    }
    
    @PostMapping("/{id}/execute")
    public ResponseEntity<ProcessInstance> executeWorkflow(@PathVariable UUID id, 
                                                          @RequestBody ExecuteRequest request) {
        ProcessInstance instance = deploymentService.executeWorkflow(id, request);
        return ResponseEntity.ok(instance);
    }
}
