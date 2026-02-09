import pg from 'pg';
import dotenv from 'dotenv';

// Ensure environment variables are loaded
dotenv.config();

const { Pool } = pg;

// Validate DATABASE_URL is loaded
if (!process.env.DATABASE_URL) {
  throw new Error('DATABASE_URL environment variable is not set');
}

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: process.env.NODE_ENV === 'production' ? { rejectUnauthorized: false } : false,
});

export const query = (text, params) => pool.query(text, params);

export const initDatabase = async () => {
  await query(`
    CREATE TABLE IF NOT EXISTS tenants (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      name VARCHAR(255) NOT NULL,
      slug VARCHAR(100) UNIQUE NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      features JSONB DEFAULT '{}',
      onboarding_completed BOOLEAN DEFAULT false,
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW()
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS workflows (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
      name VARCHAR(255) NOT NULL,
      type VARCHAR(50) NOT NULL,
      bpmn_xml TEXT,
      dmn_xml TEXT,
      version INTEGER DEFAULT 1,
      status VARCHAR(50) DEFAULT 'draft',
      created_at TIMESTAMP DEFAULT NOW(),
      updated_at TIMESTAMP DEFAULT NOW(),
      UNIQUE(tenant_id, name, version)
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS deployments (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
      workflow_id UUID REFERENCES workflows(id) ON DELETE CASCADE,
      environment VARCHAR(50) NOT NULL,
      deployment_key VARCHAR(255),
      status VARCHAR(50) DEFAULT 'pending',
      rollout_percentage INTEGER DEFAULT 0,
      deployed_at TIMESTAMP DEFAULT NOW(),
      promoted_at TIMESTAMP,
      metadata JSONB DEFAULT '{}'
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS workflow_executions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE,
      workflow_id UUID REFERENCES workflows(id) ON DELETE CASCADE,
      environment VARCHAR(50) NOT NULL,
      instance_key VARCHAR(255),
      input_data JSONB,
      output_data JSONB,
      status VARCHAR(50) DEFAULT 'running',
      started_at TIMESTAMP DEFAULT NOW(),
      completed_at TIMESTAMP
    )
  `);

  console.log('Database initialized');
};

export default pool;
