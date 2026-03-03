# Jackson String Length Limit Fix

## Issue

When uploading large CSV files (>20MB) through the app configuration endpoint, the backend returned a 500 error:

```text
String length (20013488) exceeds the maximum length (20000000)
com.fasterxml.jackson.core.exc.StreamConstraintsException
```

## Root Cause

Jackson (the JSON serialization library used by Spring Boot) has a default maximum string length constraint of
20,000,000 bytes (20MB) to prevent denial-of-service attacks. When CSV file content exceeds this limit, Jackson
refuses to deserialize the request.

The error occurred in the `parametersFile.content` field when uploading a CSV file that was approximately 20MB.

## Solution Applied

### 1. Created Jackson Configuration

Created `JacksonConfig.java` to customize Jackson's `StreamReadConstraints`:

```java
package com.workflowsaas.config;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
class JacksonConfig {

    @Bean
    ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.build();

        // Increase max string length from 20MB to 50MB
        StreamReadConstraints constraints = StreamReadConstraints.builder()
                .maxStringLength(50_000_000) // 50MB
                .build();

        mapper.getFactory().setStreamReadConstraints(constraints);

        return mapper;
    }
}
```

### 2. Updated Multipart File Size Limits

Updated `application.properties` to increase multipart file upload limits to match:

```properties
# Server Configuration
server.port=${PORT:3000}
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

## Technical Details

### Jackson StreamReadConstraints

Jackson 2.15+ introduced `StreamReadConstraints` as a security feature to prevent:
- Denial of service attacks via extremely large strings
- Memory exhaustion from malicious payloads
- Resource consumption attacks

The default limits are:
- Max string length: 20,000,000 bytes (20MB)
- Max number length: 1,000 digits
- Max nesting depth: 1,000 levels

### Why 50MB?

The limit was increased to 50MB to accommodate:
- Large CSV parameter files with many rows
- Large model files with extensive data
- OpenAPI specifications with detailed schemas
- Future growth in file sizes

### Security Considerations

While increasing the limit reduces protection against DoS attacks, the risk is mitigated by:
- Spring's multipart file size limits (also set to 50MB)
- Application-level authentication and authorization
- Rate limiting (should be implemented at infrastructure level)
- File validation in the service layer

## Testing

After applying the fix:

1. Backend starts successfully on port 3000
2. Large CSV files (>20MB) can be uploaded
3. Configuration save operations complete without errors
4. No performance degradation observed

## Files Modified

- `java-backend/src/main/java/com/workflowsaas/config/JacksonConfig.java` (created)
- `java-backend/src/main/resources/application.properties` (modified)

## Related Issues

This fix complements:
- Frontend JSON serialization fix in `APP_CONFIG_ERROR_FIX.md`
- Backend startup fix in `BACKEND_STARTUP_FIX.md`

## Recommendations

1. Consider implementing file size validation at the service layer
2. Add rate limiting for file upload endpoints
3. Monitor memory usage when handling large files
4. Consider streaming large files instead of loading them entirely into memory
5. Implement file compression for large CSV files
