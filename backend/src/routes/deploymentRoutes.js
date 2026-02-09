import express from 'express';
import { authenticate, tenantIsolation } from '../middleware/auth.js';
import { query } from '../models/database.js';
import { DeploymentService } from '../services/deploymentService.js';

const router = express.Router();
const deploymentService = new DeploymentService();

router.use(authenticate, tenantIsolation);

router.post('/', async (req, res, next) => {
  try {
    const { workflowId, environment, rolloutPercentage = 0 } = req.body;
    
    const deployment = await deploymentService.deploy(
      workflowId,
      req.tenantId,
      environment,
      rolloutPercentage
    );
    
    res.status(201).json(deployment);
  } catch (error) {
    next(error);
  }
});

router.get('/', async (req, res, next) => {
  try {
    const { environment } = req.query;
    
    let queryText = 'SELECT d.*, w.name as workflow_name FROM deployments d JOIN workflows w ON d.workflow_id = w.id WHERE d.tenant_id = $1';
    const params = [req.tenantId];
    
    if (environment) {
      queryText += ' AND d.environment = $2';
      params.push(environment);
    }
    
    queryText += ' ORDER BY d.deployed_at DESC';
    
    const result = await query(queryText, params);
    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.post('/:id/promote', async (req, res, next) => {
  try {
    const { targetEnvironment, rolloutPercentage = 100 } = req.body;
    
    const promotion = await deploymentService.promote(
      req.params.id,
      req.tenantId,
      targetEnvironment,
      rolloutPercentage
    );
    
    res.json(promotion);
  } catch (error) {
    next(error);
  }
});

router.post('/:id/rollout', async (req, res, next) => {
  try {
    const percentage = req.body.percentage;
    
    const result = await query(
      `UPDATE deployments 
       SET rollout_percentage = $1
       WHERE id = $2 AND tenant_id = $3
       RETURNING *`,
      [percentage, req.params.id, req.tenantId]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Deployment not found' });
    }
    
    res.json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.post('/:id/execute', async (req, res, next) => {
  try {
    const { inputData } = req.body;
    
    const processInstance = await deploymentService.executeWorkflow(
      req.params.id,
      req.tenantId,
      inputData
    );
    
    res.json(processInstance);
  } catch (error) {
    next(error);
  }
});

export default router;
