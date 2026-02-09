import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import tenantRoutes from './routes/tenantRoutes.js';
import workflowRoutes from './routes/workflowRoutes.js';
import deploymentRoutes from './routes/deploymentRoutes.js';
import { errorHandler } from './middleware/errorHandler.js';
import { initDatabase } from './models/database.js';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json({ limit: '10mb' }));

app.use('/api/tenants', tenantRoutes);
app.use('/api/workflows', workflowRoutes);
app.use('/api/deployments', deploymentRoutes);

app.get('/health', (req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

app.use(errorHandler);

// Initialize database and start server
initDatabase()
  .then(() => {
    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
    });
  })
  .catch((error) => {
    console.error('Failed to initialize database:', error);
    process.exit(1);
  });
