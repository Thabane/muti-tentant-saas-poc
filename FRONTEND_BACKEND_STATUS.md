# Frontend-Backend Connection Status

## Current Status ✅

### Java Backend
- **Status**: ✅ RUNNING
- **Port**: 3000
- **Health Check**: http://localhost:3000/health - RESPONDING
- **Database**: Connected to PostgreSQL (localhost:5432)
- **CORS**: Configured for http://localhost:5173

### Infrastructure
- **PostgreSQL**: ✅ RUNNING (port 5432)
- **Camunda**: ✅ RUNNING (port 8080)

### Frontend
- **Status**: ❌ NOT RUNNING (needs Node.js/npm)
- **Expected Port**: 5173
- **API Proxy**: Configured to proxy `/api` to `http://localhost:3000`

## Configuration

### Java Backend (.env)
```
PORT=3000
NODE_ENV=development
DATABASE_URL=jdbc:postgresql://localhost:5432/workflow_saas?user=user&password=password
JWT_SECRET=your-secret-key-change-in-production
CAMUNDA_REST_URL=http://localhost:8080/engine-rest
CORS_ORIGINS=http://localhost:5173
```

### Frontend (vite.config.js)
```javascript
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:3000',
      changeOrigin: true
    }
  }
}
```

## Next Steps to Get Frontend Working

### Option 1: Install Node.js (Recommended)
1. Install Node.js from https://nodejs.org/ (LTS version)
2. Restart your terminal/PowerShell
3. Run:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### Option 2: Use Alternative Shell
If Node.js is installed but not in PowerShell PATH:
1. Open Command Prompt (cmd.exe)
2. Navigate to frontend directory
3. Run `npm run dev`

### Option 3: Add Node.js to PATH
1. Find Node.js installation directory (usually `C:\Program Files\nodejs`)
2. Add to System PATH environment variable
3. Restart PowerShell
4. Run `npm run dev` in frontend directory

## Testing the Connection

Once frontend is running, test these endpoints:

1. **Health Check**
   ```
   GET http://localhost:3000/health
   ```

2. **Register** (via frontend at http://localhost:5173/register)
   ```
   POST http://localhost:3000/api/tenants/register
   Body: { "name": "Test Org", "slug": "test-org", "email": "test@example.com", "password": "password123" }
   ```

3. **Login** (via frontend at http://localhost:5173/login)
   ```
   POST http://localhost:3000/api/tenants/login
   Body: { "email": "test@example.com", "password": "password123" }
   ```

## API Endpoints Available

All endpoints are accessible at `http://localhost:3000/api/`:

### Tenants
- POST `/api/tenants/register` - Register new tenant
- POST `/api/tenants/login` - Login
- GET `/api/tenants/me` - Get current tenant (authenticated)
- PATCH `/api/tenants/onboarding` - Complete onboarding (authenticated)

### Workflows
- POST `/api/workflows` - Create workflow (authenticated)
- GET `/api/workflows` - List workflows (authenticated)
- GET `/api/workflows/:id` - Get workflow (authenticated)
- PUT `/api/workflows/:id` - Update workflow (authenticated)
- POST `/api/workflows/:id/test` - Test run workflow (authenticated)

### Deployments
- POST `/api/deployments` - Create deployment (authenticated)
- GET `/api/deployments` - List deployments (authenticated)
- POST `/api/deployments/:id/promote` - Promote deployment (authenticated)
- POST `/api/deployments/:id/rollout` - Update rollout percentage (authenticated)
- POST `/api/deployments/:id/execute` - Execute workflow (authenticated)

## Troubleshooting

### Frontend not connecting to backend
1. Check Java backend is running: `curl http://localhost:3000/health`
2. Check CORS configuration in `java-backend/src/main/resources/application.properties`
3. Check browser console for CORS errors
4. Verify Vite proxy configuration in `frontend/vite.config.js`

### Database connection errors
1. Verify PostgreSQL is running: `docker ps`
2. Check database credentials in `.env` file
3. Test connection: `psql -h localhost -U user -d workflow_saas`

### JWT token issues
1. Ensure JWT_SECRET matches between Node.js and Java backends
2. Check token expiration (7 days)
3. Clear browser localStorage and re-login
