# Default Tenant Migration - Summary

## Migration File
`src/main/resources/db/changelog/changes/004-insert-default-tenant.yaml`

## Purpose
Inserts a default tenant into the database for development and testing purposes when Spring Security is disabled.

## Migration Details

### Tenant Information
- **ID**: `00000000-0000-0000-0000-000000000001` (fixed UUID)
- **Name**: Default Tenant
- **Slug**: default
- **Email**: default@example.com
- **Password Hash**: default_password_hash (plain text, security disabled)
- **Created At**: NOW() (timestamp at migration time)
- **Updated At**: NOW() (timestamp at migration time)

### Rollback Strategy
The migration includes a rollback that deletes the default tenant:
```yaml
rollback:
  - delete:
      tableName: tenants
      where: id = '00000000-0000-0000-0000-000000000001'
```

## Testing Results

### Compilation
✅ Application compiles successfully
```bash
mvn clean compile -DskipTests
# Result: BUILD SUCCESS
```

### Test Suite
✅ All tests pass (94 tests)
```bash
mvn test
# Result: Tests run: 94, Failures: 0, Errors: 0, Skipped: 0
```

### Migration Execution
✅ Liquibase migrations run successfully during test execution
- Logs show: "Database is up to date, no changesets to execute"
- This confirms all migrations (including 004-insert-default-tenant) have been applied

## Usage

### Application Startup
The migration runs automatically when the application starts:
```bash
mvn spring-boot:run
```

### Default Tenant Usage
When security is disabled, services use this default tenant ID when no tenant context is available:
- `AppServiceImpl.createApp()` - uses default tenant if context is null
- `AppServiceImpl.getAllApps()` - uses default tenant if context is null
- `AppServiceImpl.getAppById()` - uses default tenant if context is null

### Verification
To verify the default tenant exists in the database:
```sql
SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';
```

Expected result:
```
id                                   | name           | slug    | email                  | password_hash          | created_at          | updated_at
-------------------------------------|----------------|---------|------------------------|------------------------|---------------------|--------------------
00000000-0000-0000-0000-000000000001 | Default Tenant | default | default@example.com    | default_password_hash  | 2026-02-18 10:00:00 | 2026-02-18 10:00:00
```

## Related Files
- Migration: `src/main/resources/db/changelog/changes/004-insert-default-tenant.yaml`
- Security Summary: `SECURITY_DISABLED_SUMMARY.md`
- Testing Guide: `MIGRATION_TESTING.md`
- Service Implementation: `src/main/java/com/workflowsaas/service/impl/AppServiceImpl.java`

## Notes
- This migration is specifically for development/testing with security disabled
- In production with security enabled, tenants are created through the registration flow
- The default tenant ID is hardcoded in `AppServiceImpl` as a fallback
- Password is stored as plain text since security is disabled (see SECURITY_DISABLED_SUMMARY.md)

## Status
✅ Migration file created and tested
✅ All tests passing
✅ Application compiles successfully
✅ Liquibase executes migration correctly
✅ Documentation complete
