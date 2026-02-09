import express from 'express';
import { authenticate, tenantIsolation } from '../middleware/auth.js';
import { query } from '../models/database.js';
import { WorkflowService } from '../services/workflowService.js';

const router = express.Router();
const workflowService = new WorkflowService();

router.use(authenticate, tenantIsolation);

router.post('/', async (req, res, next) => {
  try {
    const { name, type, bpmn_xml, dmn_xml } = req.body;
    
    const result = await query(
      `INSERT INTO workflows (tenant_id, name, type, bpmn_xml, dmn_xml)
       VALUES ($1, $2, $3, $4, $5) RETURNING *`,
      [req.tenantId, name, type, bpmn_xml, dmn_xml]
    );
    
    res.status(201).json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.get('/', async (req, res, next) => {
  try {
    const result = await query(
      'SELECT * FROM workflows WHERE tenant_id = $1 ORDER BY created_at DESC',
      [req.tenantId]
    );
    
    res.json(result.rows);
  } catch (error) {
    next(error);
  }
});

router.get('/:id', async (req, res, next) => {
  try {
    const result = await query(
      'SELECT * FROM workflows WHERE id = $1 AND tenant_id = $2',
      [req.params.id, req.tenantId]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Workflow not found' });
    }
    
    res.json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.put('/:id', async (req, res, next) => {
  try {
    const { name, bpmn_xml, dmn_xml } = req.body;
    
    const result = await query(
      `UPDATE workflows 
       SET name = COALESCE($1, name), 
           bpmn_xml = COALESCE($2, bpmn_xml),
           dmn_xml = COALESCE($3, dmn_xml),
           updated_at = NOW()
       WHERE id = $4 AND tenant_id = $5
       RETURNING *`,
      [name, bpmn_xml, dmn_xml, req.params.id, req.tenantId]
    );
    
    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Workflow not found' });
    }
    
    res.json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.post('/:id/test', async (req, res, next) => {
  try {
    const { inputData } = req.body;
    
    const workflowResult = await query(
      'SELECT * FROM workflows WHERE id = $1 AND tenant_id = $2',
      [req.params.id, req.tenantId]
    );
    
    if (workflowResult.rows.length === 0) {
      return res.status(404).json({ error: 'Workflow not found' });
    }
    
    const workflow = workflowResult.rows[0];
    const execution = await workflowService.testRun(workflow, inputData, req.tenantId);
    
    res.json(execution);
  } catch (error) {
    next(error);
  }
});

export default router;
