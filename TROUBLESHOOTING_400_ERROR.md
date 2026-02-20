# Troubleshooting 400 Error When Creating App

## Error
```
Apps.jsx:41 Error creating app: AxiosError: Request failed with status code 400
```

## Possible Causes

### 1. Backend Not Running
The most common cause. Verify the backend is running:

```bash
# Check if backend is running on port 3000
curl http://localhost:3000/actuator/health

# Expected response:
# {"status":"UP"}
```

If not running, start it:
```bash
cd java-backend
mvn spring-boot:run
```

### 2. Validation Error
The request might be failing validation. Check:
- App name is not empty
- App name is not just whitespace

### 3. Database Connection Issue
The backend might not be able to connect to PostgreSQL.

Check if PostgreSQL is running:
```bash
docker-compose ps
```

If not running:
```bash
docker-compose up -d postgres
```

### 4. CORS Issue
Although CORS is configured, verify it's working:
```bash
curl -X OPTIONS http://localhost:3000/api/apps \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

## Testing the Endpoint Directly

### Test with curl
```bash
# Test creating an app
curl -X POST http://localhost:3000/api/apps \
  -H "Content-Type: application/json" \
  -d '{"name":"Test App"}' \
  -v
```

Expected response (201 Created):
```json
{
  "id": "uuid-here",
  "name": "Test App",
  "apiKey": "app_xxxxxxxxxxxxx",
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T10:00:00",
  "workflows": []
}
```

### Test with invalid data
```bash
# Test with empty name (should return 400)
curl -X POST http://localhost:3000/api/apps \
  -H "Content-Type: application/json" \
  -d '{"name":""}' \
  -v
```

Expected response (400 Bad Request):
```json
{
  "message": "name: App name is required",
  "stackTrace": "..."
}
```

## Debugging Steps

### 1. Check Backend Logs
Look for errors in the backend console output:
- Validation errors
- Database connection errors
- Stack traces

### 2. Check Browser Console
Open browser DevTools (F12) and check:
- Network tab: Look at the request/response details
- Console tab: Look for error messages

### 3. Check Request Payload
In browser DevTools Network tab:
- Click on the failed request
- Check "Payload" tab
- Verify it shows: `{"name":"Your App Name"}`

### 4. Check Response
In browser DevTools Network tab:
- Click on the failed request
- Check "Response" tab
- Look for the error message from the backend

## Common Issues and Solutions

### Issue: "App name is required"
**Cause**: The name field is empty or whitespace
**Solution**: Ensure you enter a non-empty name in the form

### Issue: "Connection refused"
**Cause**: Backend is not running
**Solution**: Start the backend with `mvn spring-boot:run`

### Issue: "Database connection failed"
**Cause**: PostgreSQL is not running
**Solution**: Start PostgreSQL with `docker-compose up -d postgres`

### Issue: "CORS error"
**Cause**: CORS not configured properly
**Solution**: Verify `NODE_ENV=development` in `.env` file

## Enhanced Logging

I've added enhanced logging to help debug:

### AppController
Now logs:
- Received request
- App name value

### GlobalExceptionHandler
Now logs:
- Validation errors with field names
- Stack traces in development mode

### Frontend (Apps.jsx)
Now logs:
- Request being sent
- Response received
- Full error details

## Next Steps

1. **Start the backend** if not running:
   ```bash
   cd java-backend
   mvn spring-boot:run
   ```

2. **Check the backend logs** for any errors when you try to create an app

3. **Check the browser console** for the detailed error message

4. **Try the curl command** to test the endpoint directly

5. **Verify the database** is running and accessible

## Expected Behavior

When working correctly:
1. User enters app name in modal
2. Frontend sends POST request to `/api/apps` with `{"name":"App Name"}`
3. Backend validates the request
4. Backend creates app with generated API key
5. Backend returns 201 Created with app details
6. Frontend closes modal and refreshes app list
