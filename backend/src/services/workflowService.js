import { v4 as uuidv4 } from 'uuid';
import { query } from '../models/database.js';
import axios from 'axios';

const CAMUNDA_REST_URL = process.env.CAMUNDA_REST_URL || 'http://localhost:8080/engine-rest';

export class WorkflowService {
  async testRun(workflow, inputData, tenantId) {
    const executionId = uuidv4();
    
    await query(
      `INSERT INTO workflow_executions 
       (id, tenant_id, workflow_id, environment, input_data, status)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [executionId, tenantId, workflow.id, 'test', inputData, 'running']
    );
    
    try {
      // For test environment, simulate execution without calling Camunda
      const output = await this.simulateExecution(workflow, inputData);
      
      await query(
        `UPDATE workflow_executions 
         SET status = $1, output_data = $2, completed_at = NOW()
         WHERE id = $3`,
        ['completed', output, executionId]
      );
      
      return {
        executionId,
        status: 'completed',
        input: inputData,
        output,
        workflow: { id: workflow.id, name: workflow.name }
      };
    } catch (error) {
      await query(
        `UPDATE workflow_executions 
         SET status = $1, output_data = $2, completed_at = NOW()
         WHERE id = $3`,
        ['failed', { error: error.message }, executionId]
      );
      
      throw error;
    }
  }
  
  async simulateExecution(workflow, inputData) {
    // Simulate workflow execution for test environment
    return {
      message: 'Workflow simulation completed',
      processedData: inputData,
      timestamp: new Date().toISOString(),
      steps: [
        { name: 'Start', status: 'completed' },
        { name: 'Process', status: 'completed' },
        { name: 'End', status: 'completed' }
      ]
    };
  }

  async deployToCamunda(bpmnXml, deploymentName, tenantId) {
    try {
      // Ensure BPMN has historyTimeToLive attribute
      const validatedBpmn = this.ensureHistoryTimeToLive(bpmnXml);
      
      const FormData = (await import('form-data')).default;
      const formData = new FormData();
      
      formData.append('deployment-name', deploymentName);
      formData.append('enable-duplicate-filtering', 'false');
      formData.append('deploy-changed-only', 'false');
      formData.append('tenant-id', tenantId);
      formData.append('diagram.bpmn', Buffer.from(validatedBpmn), {
        filename: 'diagram.bpmn',
        contentType: 'text/xml'
      });

      const response = await axios.post(
        `${CAMUNDA_REST_URL}/deployment/create`,
        formData,
        {
          headers: formData.getHeaders()
        }
      );

      return response.data;
    } catch (error) {
      console.error('Camunda deployment error:', error.response?.data || error.message);
      throw new Error(`Failed to deploy to Camunda: ${error.message}`);
    }
  }

  ensureHistoryTimeToLive(bpmnXml) {
    // Check if historyTimeToLive is already present
    if (bpmnXml.includes('camunda:historyTimeToLive')) {
      return bpmnXml;
    }

    // Add camunda namespace if not present
    let updatedXml = bpmnXml;
    if (!updatedXml.includes('xmlns:camunda')) {
      updatedXml = updatedXml.replace(
        '<bpmn:definitions',
        '<bpmn:definitions xmlns:camunda="http://camunda.org/schema/1.0/bpmn"'
      );
    }

    // Add historyTimeToLive to process elements
    updatedXml = updatedXml.replace(
      /<bpmn:process([^>]*?)>/g,
      (match, attributes) => {
        if (attributes.includes('camunda:historyTimeToLive')) {
          return match;
        }
        return `<bpmn:process${attributes} camunda:historyTimeToLive="180">`;
      }
    );

    return updatedXml;
  }

  async startProcessInstance(processDefinitionKey, variables, tenantId) {
    try {
      const response = await axios.post(
        `${CAMUNDA_REST_URL}/process-definition/key/${processDefinitionKey}/tenant-id/${tenantId}/start`,
        {
          variables: this.convertToProcessVariables(variables),
          businessKey: `instance-${Date.now()}`
        }
      );

      return response.data;
    } catch (error) {
      console.error('Process start error:', error.response?.data || error.message);
      throw new Error(`Failed to start process: ${error.message}`);
    }
  }

  async getProcessInstance(processInstanceId) {
    try {
      const response = await axios.get(
        `${CAMUNDA_REST_URL}/process-instance/${processInstanceId}`
      );
      return response.data;
    } catch (error) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  }

  async getProcessInstanceHistory(processInstanceId) {
    try {
      const response = await axios.get(
        `${CAMUNDA_REST_URL}/history/process-instance/${processInstanceId}`
      );
      return response.data;
    } catch (error) {
      console.error('History fetch error:', error.response?.data || error.message);
      return null;
    }
  }

  convertToProcessVariables(data) {
    const variables = {};
    for (const [key, value] of Object.entries(data)) {
      variables[key] = {
        value: value,
        type: this.inferType(value)
      };
    }
    return variables;
  }

  inferType(value) {
    if (typeof value === 'string') return 'String';
    if (typeof value === 'number') return Number.isInteger(value) ? 'Integer' : 'Double';
    if (typeof value === 'boolean') return 'Boolean';
    if (value === null) return 'Null';
    return 'Json';
  }
}
