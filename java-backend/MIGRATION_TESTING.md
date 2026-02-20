# Liquibase Migration Testing Guide

## Overview

Liquibase migrations are database schema changes that run automatically when the application starts. Unlike application code, migrations are best tested through integration testing or by verifying the application starts successfully.

## Testing Approach for Migrations

### 1. Application Startup Test
The primary way to verify migrations work correctly:

```bash
# Start the application
mvn spring-boot:run

# Check logs for successful migration
# Look for: "Liquibase: Successfully applied X changeset(s)"
```

### 2. Manual Database Verification
After application startup, connect to the database and verify:

```sql
-- Check if default tenant exists
SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';

-- Verify all expected columns
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'tenants';

-- Check Liquibase tracking table
SELECT * FROM databasechangelog 
WHERE id = '004-insert-default-tenant';
```

### 3. Integration Tests
Integration tests with `@SpringBootTest` will automatically run migrations:

```java
@SpringBootTest
@ActiveProfiles("test")
class ApplicationIntegrationTest {
    
    @Autowired
    private TenantRepository tenantRepository;
    
    @Test
    void testDefaultTenantExistsAfterMigration() {
        UUID defaultId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Optional<Tenant> tenant = tenantRepository.findById(defaultId);
        assertTrue(tenant.isPresent());
        assertEquals("Default Tenant", tenant.get().getName());
    }
}
```

## Migration File: 004-insert-default-tenant.yaml

### Purpose
Inserts a default tenant for development and testing when security is disabled.

### Details
- **ID**: `00000000-0000-0000-0000-000000000001`
- **Name**: Default Tenant
- **Slug**: default
- **Email**: default@example.com
- **Password**: default_password_hash (plain text, security disabled)

### Rollback
The migration includes a rollback that deletes the default tenant:
```yaml
rollback:
  - delete:
      tableName: tenants
      where: id = '00000000-0000-0000-0000-000000000001'
```

## Common Issues

### Migration Already Applied
If you need to re-run a migration:
```sql
-- Remove from tracking table
DELETE FROM databasechangelog WHERE id = '004-insert-default-tenant';

-- Then restart application
```

### Duplicate Key Error
If the default tenant already exists:
```sql
-- Check if it exists
SELECT * FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';

-- If needed, delete it
DELETE FROM tenants WHERE id = '00000000-0000-0000-0000-000000000001';
```

## Best Practices

1. **Always include rollback**: Every migration should have a rollback strategy
2. **Test locally first**: Run migrations on local database before committing
3. **Use idempotent operations**: Migrations should be safe to run multiple times
4. **Version control**: Never modify existing migrations, create new ones instead
5. **Backup before production**: Always backup production database before running migrations

## Verification Checklist

- [ ] Application starts without errors
- [ ] Liquibase logs show successful changeset application
- [ ] Default tenant exists in database with correct values
- [ ] All tenant columns are present (including features, onboarding_completed)
- [ ] Rollback works correctly
- [ ] No duplicate key violations
