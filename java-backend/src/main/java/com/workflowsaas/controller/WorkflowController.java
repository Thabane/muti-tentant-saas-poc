package com.workflowsaas.controller;

import com.workflowsaas.dto.request.CreateWorkflowRequest;
import com.workflowsaas.dto.request.TestRunRequest;
import com.workflowsaas.dto.request.UpdateWorkflowRequest;
import com.workflowsaas.dto.response.WorkflowExecutionResult;
import com.workflowsaas.entity.Workflow;
import com.workflowsaas.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for workflow management endpoints.
 */
@RestController
@RequestMapping("/api/workflows")
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }
    
    @PostMapping
    public ResponseEntity<Workflow> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        Workflow workflow = workflowService.createWorkflow(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workflow);
    }
    
    @GetMapping
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        List<Workflow> workflows = workflowService.getAllWorkflows();
        return ResponseEntity.ok(workflows);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflow(@PathVariable UUID id) {
        Workflow workflow = workflowService.getWorkflowById(id);
        return ResponseEntity.ok(workflow);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable UUID id, 
                                                   @RequestBody UpdateWorkflowRequest request) {
        Workflow workflow = workflowService.updateWorkflow(id, request);
        return ResponseEntity.ok(workflow);
    }
    
    @PostMapping("/{id}/test")
    public ResponseEntity<WorkflowExecutionResult> testRun(@PathVariable UUID id, 
                                                          @RequestBody TestRunRequest request) {
        WorkflowExecutionResult result = workflowService.testRun(id, request);
        return ResponseEntity.ok(result);
    }
}
