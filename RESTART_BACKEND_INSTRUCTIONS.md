# Backend Restart Instructions

## Problem
The backend is still returning 401 Unauthorized errors because it's running with the old Camunda webapp dependency that includes Spring Security.

## Solution
You need to completely rebuild and restart the backend with the updated pom.xml (which has the webapp dependency commented out).

## Steps

### 1. Stop the Current Backend
Find the terminal where the backend is running and press `Ctrl+C` to stop it.

Or kill the process manually:
```bash
# Find the process on port 3000
netstat -ano | findstr :3000

# Kill it (replace PID with the actual process ID from above)
taskkill /PID <PID> /F
```

### 2. Clean Maven Cache
```bash
cd java-backend
mvn clean
```

### 3. Rebuild with Updated Dependencies
```bash
mvn install -DskipTests
```

This will:
- Download dependencies based on the updated pom.xml
- Remove the Camunda webapp dependency
- Remove Spring Security (which came with the webapp)
- Compile the application

### 4. Verify the Build
Check the build output for:
- `BUILD SUCCESS`
- No errors about missing Spring Security classes

### 5. Start the Backend
```bash
mvn spring-boot:run
```

### 6. Verify It's Working
In a new terminal, test the API:
```bash
curl http://localhost:3000/api/workflows
```

You should get a `200 OK` response with an empty array `[]` instead of `401 Unauthorized`.

## What Changed

The `camunda-bpm-spring-boot-starter-webapp` dependency has been removed from pom.xml because:
1. It includes Spring Security which requires authentication
2. We want to run without authentication for development
3. The webapp (Cockpit/Tasklist/Admin UI) can be accessed via the standalone Camunda Docker container instead

## If Still Getting 401

If you still get 401 errors after following these steps:

1. Check that pom.xml line ~111 has the webapp dependency commented out (between `<!--` and `-->`)
2. Delete the `java-backend/target` directory completely
3. Run `mvn clean install -DskipTests` again
4. Make sure no other instance of the backend is running on port 3000
