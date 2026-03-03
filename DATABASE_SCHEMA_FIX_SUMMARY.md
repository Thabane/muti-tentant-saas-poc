# Database Schema Fix Summary

## Issue

The application was failing to start with a 500 Internal Server Error on the Dashboard page. The error was:

```
ERROR: column t1_0.configuration does not exist
```

## Root Cause

The `Tenant` entity in the Java backend had a `configuration` field mapped to a JSONB column:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
private Map<String, Object> configuration = new HashMap<>();
```

However, the database migration `003-add-tenant-features.yaml` only added the `features` and `onboarding_completed` columns, but not the `configuration` column.

## Solution

1. **Created new migration**: Added `007-add-tenant-configuration-column.yaml` to add the missing `configuration` column with proper preconditions to avoid conflicts.

2. **Manual database update**: Since the column was needed immediately, also manually added it to the database:
   ```sql
   ALTER TABLE tenants ADD COLUMN IF NOT EXISTS configuration jsonb DEFAULT '{}'::jsonb;
   ```

3. **Updated master changelog**: Added the new migration to `db.changelog-master.yaml`.

## Files Modified

- Created: `java-backend/src/main/resources/db/changelog/changes/007-add-tenant-configuration-column.yaml`
- Modified: `java-backend/src/main/resources/db/changelog/db.changelog-master.yaml`

## Migration Content

```yaml
databaseChangeLog:
  - changeSet:
      id: 007-add-tenant-configuration-column
      author: system
      changes:
        - addColumn:
            tableName: tenants
            columns:
              - column:
                  name: configuration
                  type: jsonb
                  defaultValueComputed: "'{}'::jsonb"
      preConditions:
        - onFail: MARK_RAN
        - not:
            - columnExists:
                tableName: tenants
                columnName: configuration
```

The `preConditions` ensure that if the column already exists (from the manual update), the changeset will be marked as ran without attempting to add it again.

## Verification

Backend started successfully on port 3000:
```
Started Application in 9.016 seconds (process running for 9.593)
Tomcat started on port(s): 3000 (http) with context path ''
```

The Dashboard page should now load without errors.
