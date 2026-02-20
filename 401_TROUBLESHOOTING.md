# 401 Unauthorized Error - Troubleshooting Guide

## Current Situation
The backend is returning 401 Unauthorized errors because Spring Security is still active in the running application, even though we've commented out the `camunda-bpm-spring-boot-starter-webapp` dependency in pom.xml.

## Root Cause
The backend JAR file in `java-backend/target/` was built with the OLD pom.xml that included the webapp dependency. Spring Security JARs are bundled in this old JAR and are still being used by the running application.

## Verification
I checked and confirmed Spring Security is still present:
```
BOOT-INF/lib/spring-security-config-6.1.5.jar
BOOT-INF/lib/spring-security-core-6.1.5.jar
BOOT-INF/lib/spring-security-crypto-6.1.5.jar
BOOT-INF/lib/spring-security-web-6.1.5.jar
```

## Solution: Complete Rebuild Required

### Step 1: Stop ALL Backend Processes
```bash
# Find processes on port 3000
netstat -ano | findstr :3000

# Kill the process (replace <PID> with actual process ID)
taskkill /PID <PID> /F
```

### Step 2: Delete Old Build Artifacts
```bash
cd java-backend
rmdir /s /q target
```

This removes ALL compiled code, including the old Spring Security JARs.

### Step 3: Clean Maven Local Repository Cache (Optional but Recommended)
```bash
mvn dependency:purge-local-repository -DmanualInclude=org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp
```

### Step 4: Rebuild from Scratch
```bash
mvn clean install -DskipTests
```

Watch the build output. You should see:
- No Spring Security dependencies being downloaded
- Only `camunda-bpm-spring-boot-starter` and `camunda-bpm-spring-boot-starter-rest`
- BUILD SUCCESS

### Step 5: Verify the New JAR
```bash
jar tf target\workflow-saas-1.0.0.jar | findstr spring-security
```

This should return NOTHING. If it shows Spring Security JARs, the rebuild didn't work.

### Step 6: Start the Backend
```bash
mvn spring-boot:run
```

### Step 7: Test the API
```bash
curl http://localhost:3000/api/workflows
```

Expected response: `[]` (empty array) with HTTP 200
NOT: 401 Unauthorized

## Alternative: Use Spring Boot DevTools
If rebuilding doesn't work, there might be a cached classloader issue. Try:

1. Add Spring Boot DevTools to pom.xml (if not already there)
2. Use `mvn spring-boot:run` which has automatic restart
3. Or run from IDE with hot reload enabled

## If Still Getting 401 After Rebuild

### Check 1: Verify pom.xml
```bash
type java-backend\pom.xml | findstr "camunda-bpm-spring-boot-starter-webapp"
```

The line should be inside `<!--` and `-->` comments.

### Check 2: Check for Multiple pom.xml Files
```bash
dir /s /b pom.xml
```

Make sure there's only ONE pom.xml in java-backend/.

### Check 3: Check Maven Effective POM
```bash
cd java-backend
mvn help:effective-pom | findstr "camunda-bpm-spring-boot-starter-webapp"
```

This should return NOTHING if the dependency is truly excluded.

### Check 4: Look for Spring Security Auto-Configuration
Check the startup logs for:
```
o.s.s.web.DefaultSecurityFilterChain
SecurityFilterChain
```

If you see these, Spring Security is still active.

## Nuclear Option: Exclude Spring Security Explicitly

If the webapp dependency is somehow still being pulled in transitively, add this to pom.xml:

```xml
<dependency>
    <groupId>org.camunda.bpm.springboot</groupId>
    <artifactId>camunda-bpm-spring-boot-starter</artifactId>
    <version>${camunda.version}</version>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Expected Behavior After Fix

1. Backend starts without Spring Security
2. All `/api/*` endpoints return 200 (or 404 if not found)
3. No authentication required
4. Frontend can access all APIs without tokens
5. Dashboard loads without 401 errors

## Current Status

- ✅ pom.xml updated (webapp dependency commented out)
- ✅ application.properties updated (camunda authorization disabled)
- ❌ Backend NOT rebuilt with new dependencies
- ❌ Old Spring Security JARs still in target/
- ❌ Backend still returning 401 errors

**Next Action: Follow Steps 1-7 above to rebuild completely**
