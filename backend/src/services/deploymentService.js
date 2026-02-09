import { query } from '../models/database.js';
import { WorkflowService } from './workflowService.js';

const workflowService = new WorkflowService();

export class DeploymentService {
  async deploy(workflowId, tenantId, environment, rolloutPercentage) {
    const workflowResult = await query(
      'SELECT * FROM workflows WHERE id = $1 AND tenant_id = $2',
      [workflowId, tenantId]
    );
    
    if (workflowResult.rows.length === 0) {
      throw new Error('Workflow not found');
    }
    
    const workflow = workflowResult.rows[0];
    const deploymentKey = `${tenantId}-${workflow.name}-${environment}-${Date.now()}`;
    
    let camundaDeploymentId = null;
    
    // Deploy to Camunda for non-prod and production environments
    if (environment !== 'test' && workflow.bpmn_xml) {
      try {
        const camundaDeployment = await workflowService.deployToCamunda(
          workflow.bpmn_xml,
          deploymentKey,
          tenantId
        );
        camundaDeploymentId = camundaDeployment.id;
      } catch (error) {
        console.error('Camunda deployment failed:', error);
        throw new Error(`Failed to deploy to Camunda: ${error.message}`);
      }
    }
    
    const result = await query(
      `INSERT INTO deployments 
       (tenant_id, workflow_id, environment, deployment_key, status, rollout_percentage, metadata)
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING *`,
      [
        tenantId, 
        workflowId, 
        environment, 
        deploymentKey, 
        'active', 
        rolloutPercentage,
        { camundaDeploymentId }
      ]
    );
    
    return result.rows[0];
  }
  
  async promote(deploymentId, tenantId, targetEnvironment, rolloutPercentage) {
    const deploymentResult = await query(
      'SELECT d.*, w.* FROM deployments d JOIN workflows w ON d.workflow_id = w.id WHERE d.id = $1 AND d.tenant_id = $2',
      [deploymentId, tenantId]
    );
    
    if (deploymentResult.rows.length === 0) {
      throw new Error('Deployment not found');
    }
    
    const currentDeployment = deploymentResult.rows[0];
    
    const envOrder = ['test', 'non-prod', 'production'];
    const currentIndex = envOrder.indexOf(currentDeployment.environment);
    const targetIndex = envOrder.indexOf(targetEnvironment);
    
    if (targetIndex <= currentIndex) {
      throw new Error('Can only promote to higher environments');
    }
    
    const newDeployment = await this.deploy(
      currentDeployment.workflow_id,
      tenantId,
      targetEnvironment,
      rolloutPercentage
    );
    
    await query(
      'UPDATE deployments SET promoted_at = NOW() WHERE id = $1',
      [deploymentId]
    );
    
    return newDeployment;
  }

  async executeWorkflow(deploymentId, tenantId, inputData) {
    const deploymentResult = await query(
      `SELECT d.*, w.* FROM deployments d 
       JOIN workflows w ON d.workflow_id = w.id 
       WHERE d.id = $1 AND d.tenant_id = $2`,
      [deploymentId, tenantId]
    );
    
    if (deploymentResult.rows.length === 0) {
      throw new Error('Deployment not found');
    }
    
    const deployment = deploymentResult.rows[0];
    
    if (deployment.environment === 'test') {
      throw new Error('Use test-run endpoint for test environment');
    }

    // Extract process definition key from BPMN
    const processKey = this.extractProcessKey(deployment.bpmn_xml);
    
    const processInstance = await workflowService.startProcessInstance(
      processKey,
      inputData,
      tenantId
    );

    // Record execution
    await query(
      `INSERT INTO workflow_executions 
       (tenant_id, workflow_id, environment, instance_key, input_data, status)
       VALUES ($1, $2, $3, $4, $5, $6)`,
      [
        tenantId,
        deployment.workflow_id,
        deployment.environment,
        processInstance.id,
        inputData,
        'running'
      ]
    );

    return processInstance;
  }

  extractProcessKey(bpmnXml) {
    // Simple regex to extract process id from BPMN XML
    const match = bpmnXml.match(/bpmn:process[^>]+id="([^"]+)"/);
    return match ? match[1] : 'Process_1';
  }
}
