# Debugging Steps for 400 Error

## Current Status
- ✅ Frontend is sending requests
- ✅ Backend is receiving requests (getting 400 response)
- ❌ Request is failing with 400 Bad Request
- ✅ Enhanced logging is now in place

## What to Do Next

### Step 1: Restart the Backend
The enhanced logging has been added. Restart the backend to see detailed logs:

```bash
cd java-backend
mvn spring-boot:run
```

### Step 2: Try Creating an App Again
1. Open the frontend in your browser (http://localhost:5173)
2. Navigate to the Apps page
3. Click "Create App"
4. Enter a name (e.g., "My Test App")
5. Click "Create"

### Step 3: Check the Backend Console
Look for these log messages in the backend console:

```
=== CREATE APP REQUEST ===
Received createApp request: CreateAppRequest[name=My Test App]
App name: My Test App
Name length: 11
Name is blank: false
```

If you see an error, it will show:
```
=== ERROR CREATING APP ===
Error type: [exception class]
Error message: [error message]
[stack trace]
```

### Step 4: Check the Browser Console
Open browser DevTools (F12) and look for:

```javascript
Creating app with name: My Test App
Request payload: {name: "My Test App"}
```

If there's an error:
```javascript
Error creating app: AxiosError...
Error response data: {message: "...", stackTrace: "..."}
Error message from backend: [actual error message]
```

## Common Error Messages and Solutions

### "name: App name is required"
**Cause**: The name field is empty or contains only whitespace
**Solution**: Enter a non-empty name

### "Duplicate entry - email or slug already exists"
**Cause**: An app with the same name already exists for this tenant
**Solution**: Use a different name or delete the existing app

### "Database connection failed" or "Connection refused"
**Cause**: PostgreSQL is not running
**Solution**: 
```bash
docker-compose up -d postgres
```

### "Table 'apps' doesn't exist"
**Cause**: Database migrations haven't run
**Solution**: Restart the backend - migrations run automatically on startup

### "Foreign key constraint fails"
**Cause**: The default tenant doesn't exist
**Solution**: Check if the default tenant migration ran:
```sql
SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';
```

## Testing the Endpoint Directly

If the frontend still doesn't work, test the backend directly:

```bash
# Test with curl
curl -X POST http://localhost:3000/api/apps \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test App\"}" \
  -v

# Expected success response (201):
# {
#   "id": "...",
#   "name": "Test App",
#   "apiKey": "app_xxxxx...",
#   "createdAt": "...",
#   "updatedAt": "...",
#   "workflows": []
# }

# Expected error response (400):
# {
#   "message": "name: App name is required",
#   "stackTrace": "..."
# }
```

## What the Logs Will Tell You

### If validation fails:
```
Validation error: name: App name is required
```

### If database constraint fails:
```
Error type: org.springframework.dao.DataIntegrityViolationException
Error message: could not execute statement; SQL [n/a]; constraint [...]
```

### If API key generation fails:
```
Error type: java.lang.RuntimeException
Error message: SHA-256 algorithm not available
```

### If tenant doesn't exist:
```
Error type: org.springframework.dao.DataIntegrityViolationException
Error message: ... foreign key constraint fails ... REFERENCES `tenants` ...
```

## Next Actions Based on Error

1. **If you see validation error**: Check that the app name is not empty
2. **If you see database error**: Check PostgreSQL is running and migrations have run
3. **If you see foreign key error**: Verify default tenant exists
4. **If you see no logs at all**: Backend might not be running or request isn't reaching it

## Quick Checks

```bash
# Is backend running?
curl http://localhost:3000/actuator/health

# Is PostgreSQL running?
docker-compose ps postgres

# Check database has tables:
docker-compose exec postgres psql -U user -d workflow_saas -c "\dt"

# Check default tenant exists:
docker-compose exec postgres psql -U user -d workflow_saas -c "SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';"
```

## After Finding the Error

Once you see the error message in the backend console, share it and I can provide a specific fix.
