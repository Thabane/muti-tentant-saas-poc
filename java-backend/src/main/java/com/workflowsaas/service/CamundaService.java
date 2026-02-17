package com.workflowsaas.service;

import com.workflowsaas.dto.camunda.CamundaDeployment;
import com.workflowsaas.dto.camunda.CamundaVariable;
import com.workflowsaas.dto.camunda.ProcessInstance;
import com.workflowsaas.exception.CamundaIntegrationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for Camunda BPM Platform 7 integration.
 */
@Service
public class CamundaService {
    
    private final RestTemplate restTemplate;
    private final String camundaUrl;
    
    public CamundaService(RestTemplate restTemplate, 
                         @Value("${camunda.rest.url}") String camundaUrl) {
        this.restTemplate = restTemplate;
        this.camundaUrl = camundaUrl;
    }
    
    public CamundaDeployment deployBpmn(String bpmnXml, String deploymentName, UUID tenantId) {
        try {
            String enhancedBpmn = ensureHistoryTimeToLive(bpmnXml);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("deployment-name", deploymentName);
            body.add("tenant-id", tenantId.toString());
            body.add("data", new ByteArrayResource(enhancedBpmn.getBytes()) {
                @Override
                public String getFilename() {
                    return "workflow.bpmn";
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<CamundaDeployment> response = restTemplate.postForEntity(
                camundaUrl + "/deployment/create",
                requestEntity,
                CamundaDeployment.class
            );
            
            return response.getBody();
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
            Map<String, Object> request = new HashMap<>();
            request.put("variables", convertToProcessVariables(variables));
            request.put("tenantId", tenantId.toString());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<ProcessInstance> response = restTemplate.postForEntity(
                camundaUrl + "/process-definition/key/" + processKey + "/start",
                requestEntity,
                ProcessInstance.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            throw new CamundaIntegrationException("Failed to start process instance: " + e.getMessage());
        }
    }
    
    public Map<String, CamundaVariable> convertToProcessVariables(Map<String, Object> data) {
        Map<String, CamundaVariable> variables = new HashMap<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            String type;
            
            if (value instanceof String) {
                type = "String";
            } else if (value instanceof Integer) {
                type = "Integer";
            } else if (value instanceof Double || value instanceof Float) {
                type = "Double";
            } else if (value instanceof Boolean) {
                type = "Boolean";
            } else {
                type = "Json";
            }
            
            variables.put(entry.getKey(), new CamundaVariable(value, type));
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
