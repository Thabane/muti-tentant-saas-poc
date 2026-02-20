package com.workflowsaas.service;

import com.workflowsaas.dto.camunda.CamundaDeployment;
import com.workflowsaas.dto.camunda.CamundaVariable;
import com.workflowsaas.dto.camunda.ProcessInstance;
import com.workflowsaas.exception.CamundaIntegrationException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for Camunda BPM Platform 7 embedded engine integration.
 */
@Service
public class CamundaService {
    
    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    
    public CamundaService(RepositoryService repositoryService, 
                         RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }
    
    public CamundaDeployment deployBpmn(String bpmnXml, String deploymentName, UUID tenantId) {
        try {
            String enhancedBpmn = ensureHistoryTimeToLive(bpmnXml);
            
            Deployment deployment = repositoryService.createDeployment()
                .name(deploymentName)
                .tenantId(tenantId.toString())
                .addInputStream("workflow.bpmn", new ByteArrayInputStream(enhancedBpmn.getBytes()))
                .deploy();
            
            return new CamundaDeployment(
                deployment.getId(),
                deployment.getName(),
                deployment.getDeploymentTime().toString(),
                deployment.getTenantId()
            );
        } catch (Exception e) {
            throw new CamundaIntegrationException("Failed to deploy to Camunda: " + e.getMessage());
        }
    }
    
    public String ensureHistoryTimeToLive(String bpmnXml) {
        if (bpmnXml.contains("historyTimeToLive")) {
            return bpmnXml;
        }
        
        return bpmnXml.replaceFirst(
            "(<bpmn:process[^>]*)",
            "$1 camunda:historyTimeToLive=\"180\""
        );
    }
    
    public ProcessInstance startProcessInstance(String processKey, Map<String, Object> variables, UUID tenantId) {
        try {
            Map<String, Object> processVariables = convertToProcessVariables(variables);
            
            ProcessInstanceWithVariables instance = runtimeService
                .createProcessInstanceByKey(processKey)
                .setVariables(processVariables)
                .processDefinitionTenantId(tenantId.toString())
                .executeWithVariablesInReturn();
            
            return new ProcessInstance(
                instance.getId(),
                instance.getProcessDefinitionId(),
                instance.getBusinessKey(),
                tenantId.toString(),
                instance.isEnded(),
                instance.isSuspended()
            );
        } catch (Exception e) {
            throw new CamundaIntegrationException("Failed to start process instance: " + e.getMessage());
        }
    }
    
    public Map<String, Object> convertToProcessVariables(Map<String, Object> data) {
        Map<String, Object> variables = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            TypedValue typedValue;
            
            if (value instanceof String) {
                typedValue = Variables.stringValue((String) value);
            } else if (value instanceof Integer) {
                typedValue = Variables.integerValue((Integer) value);
            } else if (value instanceof Long) {
                typedValue = Variables.longValue((Long) value);
            } else if (value instanceof Double) {
                typedValue = Variables.doubleValue((Double) value);
            } else if (value instanceof Boolean) {
                typedValue = Variables.booleanValue((Boolean) value);
            } else {
                // Complex objects as JSON
                typedValue = Variables.objectValue(value).create();
            }
            
            variables.put(entry.getKey(), typedValue);
        }
        
        return variables;
    }
    
    public String extractProcessKey(String bpmnXml) {
        Pattern pattern = Pattern.compile("<bpmn:process[^>]*id=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(bpmnXml);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        throw new CamundaIntegrationException("Could not extract process key from BPMN XML");
    }
}
