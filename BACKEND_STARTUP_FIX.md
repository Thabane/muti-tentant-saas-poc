# Backend Startup Fix

## Issue

The backend was failing to start with a Liquibase migration error:

```text
Caused by: org.postgresql.util.PSQLException: ERROR: column "features" of relation "tenants" already exists
```

## Root Cause

Two migrations were attempting to add the same columns to the `tenants` table:

1. **Migration 003** (`003-add-tenant-features.yaml`): Added `features` and `onboarding_completed` columns
2. **Migration 007** (`007-add-tenant-configuration-columns.yaml`): Attempted to add `features`, `configuration`,
and `onboarding_completed` columns again

The database already had these columns from a previous migration run, but Liquibase's changelog tracking didn't
have records of these migrations being applied. This caused Liquibase to attempt re-applying them, resulting in
duplicate column errors.

## Solution Applied

Added Liquibase preconditions to both migrations to check if columns already exist before attempting to add them:

### Migration 003 Fix

Added precondition to check if `features` column exists:

```yaml
databaseChangeLog:
  - changeSet:
      id: 003-add-tenant-features
      author: system
      preConditions:
        - onFail: MARK_RAN
        - not:
            - columnExists:
                tableName: tenants
                columnName: features
      changes:
        - addColumn:
            tableName: tenants
            columns:
              - column:
                  name: features
                  type: jsonb
              - column:
                  name: onboarding_completed
                  type: boolean
                  defaultValueBoolean: false
```

### Migration 007 Fix

Removed duplicate columns and added precondition for `configuration` column:

```yaml
databaseChangeLog:
  - changeSet:
      id: 007-add-tenant-configuration-columns
      author: system
      preConditions:
        - onFail: MARK_RAN
        - not:
            - columnExists:
                tableName: tenants
                columnName: configuration
      changes:
        - addColumn:
            tableName: tenants
            columns:
              - column:
                  name: configuration
                  type: jsonb
                  defaultValueComputed: "'{}'::jsonb"
```

## How Preconditions Work

- `preConditions`: Defines conditions that must be met before the changeset executes
- `onFail: MARK_RAN`: If precondition fails (column already exists), mark the changeset as already run without
executing it
- `not` + `columnExists`: Checks that the column does NOT exist before attempting to add it

## Result

The backend now starts successfully:

```text
Started Application in 10.153 seconds (process running for 10.974)
Tomcat started on port(s): 3000 (http) with context path ''
```

## Services Status

- **Backend**: Running on port 3000
- **Frontend**: Running on port 5173
- **Database**: PostgreSQL on port 5432

## Testing Steps

1. Navigate to http://localhost:5173
2. Log in or register a new account
3. Navigate to Apps page
4. Select or create an app
5. Click "Config" to access the configuration tab
6. Test configuration creation with the JSON serialization fix from `AppDetail.jsx`

## Related Fixes

This fix complements the frontend JSON serialization fix documented in `APP_CONFIG_ERROR_FIX.md`.
