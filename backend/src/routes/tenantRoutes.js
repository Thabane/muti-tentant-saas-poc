import express from 'express';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { query } from '../models/database.js';
import { authenticate } from '../middleware/auth.js';

const router = express.Router();

router.post('/register', async (req, res, next) => {
  try {
    const { name, slug, email, password } = req.body;
    
    const passwordHash = await bcrypt.hash(password, 10);
    
    const result = await query(
      `INSERT INTO tenants (name, slug, email, password_hash, features)
       VALUES ($1, $2, $3, $4, $5) RETURNING id, name, slug, email, features`,
      [name, slug, email, passwordHash, { workflows: true, dmn: true }]
    );
    
    const tenant = result.rows[0];
    const token = jwt.sign(
      { tenantId: tenant.id, email: tenant.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );
    
    res.status(201).json({ tenant, token });
  } catch (error) {
    next(error);
  }
});

router.post('/login', async (req, res, next) => {
  try {
    const { email, password } = req.body;
    
    const result = await query(
      'SELECT * FROM tenants WHERE email = $1',
      [email]
    );
    
    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }
    
    const tenant = result.rows[0];
    const valid = await bcrypt.compare(password, tenant.password_hash);
    
    if (!valid) {
      return res.status(401).json({ error: 'Invalid credentials' });
    }
    
    const token = jwt.sign(
      { tenantId: tenant.id, email: tenant.email },
      process.env.JWT_SECRET,
      { expiresIn: '7d' }
    );
    
    res.json({ tenant: { id: tenant.id, name: tenant.name, slug: tenant.slug, features: tenant.features }, token });
  } catch (error) {
    next(error);
  }
});

router.get('/me', authenticate, async (req, res, next) => {
  try {
    const result = await query(
      'SELECT id, name, slug, email, features, onboarding_completed FROM tenants WHERE id = $1',
      [req.tenantId]
    );
    
    res.json(result.rows[0]);
  } catch (error) {
    next(error);
  }
});

router.patch('/onboarding', authenticate, async (req, res, next) => {
  try {
    await query(
      'UPDATE tenants SET onboarding_completed = true WHERE id = $1',
      [req.tenantId]
    );
    
    res.json({ success: true });
  } catch (error) {
    next(error);
  }
});

export default router;
