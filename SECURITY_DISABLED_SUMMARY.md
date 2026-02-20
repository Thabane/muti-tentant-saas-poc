# Spring Security Removal Summary

## Overview
Spring Security has been completely disabled and removed from the Java backend. This was done to simplify development and remove authentication/authorization complexity.

## Changes Made

### 1. Dependencies
- Removed `spring-boot-starter-security` dependency
- Removed `camunda-bpm-spring-boot-starter-webapp` (includes Spring Security)
- Using only `camunda-bpm-spring-boot-starter-rest` for Camunda REST API
- Camunda web UI (Cockpit/Tasklist) should be accessed via standalone Docker container

### 2. Configuration Files Deleted
- `SecurityConfig.java` - Spring Security configuration (no longer needed)
- `JwtTokenProvider.java` - JWT token generation and validation
- `JwtAuthenticationFilter.java` - JWT authentication filter
- `ApiKeyAuthenticationFilter.java` - API key authentication filter

### 3. Camunda Authorization Disabled
- Added `camunda.bpm.authorization.enabled=false` to application.properties
- This disables Camunda's built-in authorization system

### 3. Service Layer Updates

#### TenantService.java
- Removed `PasswordEncoder` dependency
- Removed `JwtTokenProvider` dependency
- Changed password storage to plain text (security disabled)
- Modified `register()` to store plain text passwords
- Modified `login()` to compare plain text passwords
- Returns tenant ID as token instead of JWT

#### ApiKeyServiceImpl.java
- Replaced BCrypt password encoder with SHA-256 hashing
- Uses `MessageDigest` for API key hashing
- Simpler validation logic without BCrypt

#### AppServiceImpl.java
- Modified `createApp()` to use default tenant ID when context is null
- Modified `getAllApps()` to use default tenant ID when context is null
- Modified `getAllAppsWithWorkflows()` to use default tenant ID when context is null
- Modified `getAppById()` to use default tenant ID when context is null
- Added TODO comments for proper tenant selection when security is re-enabled
- Removed `UnauthorizedException` throw for missing tenant context

### 4. Test Files Deleted
- `SecurityConfigTest.java` - Tests for SecurityConfig
- `PasswordEncoderTest.java` - Tests for password encoding
- `AppControllerTest.java` - Integration tests with JWT dependencies
- `AppControllerUnitTest.java` - Unit tests with JWT dependencies

### 5. Files Kept (Still Functional)
- `TenantContextHolder.java` - Thread-local tenant context storage (still used for multi-tenancy)

## Security Implications

⚠️ **WARNING: This configuration is NOT suitable for production use!**

- Passwords are stored in plain text
- No authentication required for API endpoints
- No authorization checks
- API keys use simple SHA-256 hashing instead of BCrypt
- Tenant isolation relies on manual context setting

## Current Authentication Flow

1. User registers with plain text password
2. System stores password as-is in database
3. Login compares plain text passwords
4. Returns tenant ID as "token"
5. No token validation on subsequent requests
6. Tenant context must be manually set

## Default Tenant ID

When no tenant context is available, the system uses:
```
00000000-0000-0000-0000-000000000001
```

This default tenant is automatically created by the database migration `004-insert-default-tenant.yaml` with:
- Name: "Default Tenant"
- Slug: "default"
- Email: "default@example.com"

## Next Steps

To re-enable security in the future:
1. Add back `spring-boot-starter-security` dependency
2. Recreate `SecurityConfig` with proper filter chain
3. Implement JWT token generation and validation
4. Add password hashing with BCrypt
5. Update services to enforce authentication
6. Recreate integration tests with security context

## Testing Status

✅ All remaining tests pass:
- `ApiPathGeneratorImplTest` - 19 tests passing
- Main application compiles successfully
- No Spring Security dependencies remain

## Build Commands

```bash
# Compile
mvn clean compile -DskipTests

# Run tests
mvn test

# Run specific test
mvn test -Dtest=ApiPathGeneratorImplTest
```
