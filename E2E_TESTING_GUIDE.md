# End-to-End Testing Guide: App Configuration Management

## Test Environment Status

✅ **Backend Server**: Running on http://localhost:3000
✅ **Frontend Server**: Running on http://localhost:5173
✅ **Database**: PostgreSQL running in Docker

## Prerequisites

Before testing, ensure you have:
1. A registered user account (or register a new one)
2. At least one app created in your tenant

## Test Scenarios

### Test 1: Navigation from Apps Page to App Details

**Objective**: Verify users can navigate to the app configuration page

**Steps**:
1. Open http://localhost:5173 in your browser
2. Log in with your credentials
3. Navigate to the Apps page
4. Locate an existing app in the tree view
5. Click the "Edit" button (pencil icon) next to the app name
6. Verify you are redirected to the App Details page
7. Click on the "Configuration" tab

**Expected Results**:
- App Details page loads successfully
- App name is displayed in the header
- Configuration tab is visible and clickable
- Configuration form loads (may show empty fields if no config exists)

---

### Test 2: Create New Configuration with All Fields

**Objective**: Verify complete configuration creation workflow

**Steps**:
1. Navigate to an app's Configuration tab (from Test 1)
2. **Data Source Section**:
   - Select "eeh" from the source dropdown
   - Verify the selection is highlighted
3. **Enrichment APIs Section**:
   - Click "Add Enrichment API" button
   - Upload a valid OpenAPI JSON file (see test data below)
   - Enter request mappings: `userId -> user_id | userName -> user_name`
   - Add configuration properties:
     - Key: `baseUrl`, Value: `https://api.example.com`
     - Key: `timeout`, Value: `5000`
4. **Parameters File Section**:
   - Upload a valid CSV file (see test data below)
   - Verify filename is displayed
5. **Model File Section**:
   - Upload a valid CSV file (see test data below)
   - Verify filename is displayed
6. **Publisher Configuration Section**:
   - Select "api" from publisher type dropdown
   - Upload a valid OpenAPI JSON file
   - Enter request mappings: `result -> output`
   - Add configuration property:
     - Key: `endpoint`, Value: `https://webhook.example.com`
7. Click "Save Configuration" button

**Expected Results**:
- Success message appears: "Configuration saved successfully!"
- No validation errors are displayed
- Page remains on configuration tab
- All entered data persists after save

**Test Data**:

OpenAPI Document (save as `test-openapi.json`):
```json
{
  "openapi": "3.0.0",
  "info": {
    "title": "Test API",
    "version": "1.0.0"
  },
  "paths": {
    "/test": {
      "get": {
        "summary": "Test endpoint",
        "responses": {
          "200": {
            "description": "Success"
          }
        }
      }
    }
  }
}
```

Parameters CSV (save as `test-parameters.csv`):
```csv
name,type,defaultValue
userId,string,anonymous
maxRetries,integer,3
timeout,integer,30000
```

Model CSV (save as `test-model.csv`):
```csv
field,type,required
id,string,true
name,string,true
email,string,false
createdAt,timestamp,true
```

---

### Test 3: Update Existing Configuration

**Objective**: Verify configuration updates work correctly

**Steps**:
1. Navigate to the app with configuration from Test 2
2. Go to Configuration tab
3. Verify all previously saved data is loaded correctly
4. Make the following changes:
   - Change source from "eeh" to "api"
   - Update first enrichment API request mapping to: `userId -> id`
   - Replace parameters CSV with a different file
   - Add a second enrichment API entry
5. Click "Save Configuration"

**Expected Results**:
- Success message appears
- Changes are persisted
- Reload the page and verify changes are still present
- Old data is replaced with new data

---

### Test 4: File Upload Validation

**Objective**: Verify file validation works for all file types

**Test 4.1: Invalid JSON File**

**Steps**:
1. Navigate to Configuration tab
2. Try to upload an invalid JSON file for OpenAPI document:
   ```json
   {
     "openapi": "3.0.0",
     "info": {
       "title": "Test"
       // Missing closing brace
   ```
3. Click "Save Configuration"

**Expected Results**:
- Error message appears indicating JSON parsing error
- Error includes location information (line/column)
- Configuration is not saved

**Test 4.2: Invalid OpenAPI Structure**

**Steps**:
1. Upload a valid JSON file but invalid OpenAPI structure:
   ```json
   {
     "version": "3.0.0",
     "title": "Test"
   }
   ```
2. Click "Save Configuration"

**Expected Results**:
- Error message: "Invalid OpenAPI document: Missing required field 'openapi'"
- Configuration is not saved

**Test 4.3: Invalid CSV File**

**Steps**:
1. Upload a CSV file with inconsistent columns:
   ```csv
   name,type,value
   param1,string,test
   param2,integer
   ```
2. Click "Save Configuration"

**Expected Results**:
- Error message indicates CSV structure error with row/column information
- Configuration is not saved

**Test 4.4: Invalid Avro Schema**

**Steps**:
1. Select "eeh" as publisher type
2. Upload an invalid Avro schema:
   ```json
   {
     "name": "TestRecord",
     "type": "invalid_type"
   }
   ```
3. Click "Save Configuration"

**Expected Results**:
- Error message indicates invalid Avro schema
- Configuration is not saved

---

### Test 5: Enrichment API Management

**Objective**: Verify adding and removing enrichment APIs

**Test 5.1: Add Multiple Enrichment APIs**

**Steps**:
1. Navigate to Configuration tab
2. Click "Add Enrichment API" button 3 times
3. Configure each enrichment API with different data
4. Click "Save Configuration"

**Expected Results**:
- All 3 enrichment APIs are displayed in the UI
- Each has its own OpenAPI upload, request mappings, and config properties
- All 3 are saved successfully
- Reload page and verify all 3 are still present

**Test 5.2: Remove Enrichment API**

**Steps**:
1. From the configuration with 3 enrichment APIs
2. Click "Remove" button on the second enrichment API
3. Click "Save Configuration"
4. Reload the page

**Expected Results**:
- Only 2 enrichment APIs remain
- The correct enrichment API was removed
- Other enrichment APIs retain their data

---

### Test 6: Publisher Type Switching

**Objective**: Verify switching between API and EEH publisher types

**Steps**:
1. Navigate to Configuration tab
2. Select "api" as publisher type
3. Upload OpenAPI document and configure API publisher
4. Click "Save Configuration"
5. Reload the page
6. Change publisher type to "eeh"
7. Upload Avro schema and configure warehouse publisher
8. Click "Save Configuration"
9. Reload the page

**Expected Results**:
- When "api" is selected, API publisher fields are shown
- When "eeh" is selected, warehouse publisher fields are shown
- Configuration switches correctly between types
- Data is saved correctly for each type

**Test Data for Avro Schema** (save as `test-avro.json`):
```json
{
  "type": "record",
  "name": "TestRecord",
  "namespace": "com.example",
  "fields": [
    {
      "name": "id",
      "type": "string"
    },
    {
      "name": "timestamp",
      "type": "long"
    },
    {
      "name": "data",
      "type": {
        "type": "map",
        "values": "string"
      }
    }
  ]
}
```

---

### Test 7: Validation Error Display

**Objective**: Verify validation errors are displayed correctly

**Test 7.1: Missing Required Field**

**Steps**:
1. Navigate to Configuration tab
2. Leave source field empty
3. Click "Save Configuration"

**Expected Results**:
- Error message appears near source field: "Source is required"
- Configuration is not saved
- Other fields retain their values

**Test 7.2: Invalid Request Mapping Format**

**Steps**:
1. Add an enrichment API
2. Enter invalid request mapping: `userId = user_id` (wrong separator)
3. Click "Save Configuration"

**Expected Results**:
- Error message: "Request mapping must follow format 'source -> target'"
- Configuration is not saved

**Test 7.3: Invalid Field Names in Mappings**

**Steps**:
1. Add an enrichment API
2. Enter request mapping with invalid characters: `user-id -> user_id`
3. Click "Save Configuration"

**Expected Results**:
- Error message: "Field name 'user-id' contains invalid characters"
- Only alphanumeric, dots, and underscores are allowed

---

### Test 8: Multi-Tenant Isolation

**Objective**: Verify tenant isolation in the UI

**Prerequisites**: You need two different tenant accounts

**Steps**:
1. Log in as Tenant A
2. Create an app and configure it
3. Note the app ID from the URL
4. Log out
5. Log in as Tenant B
6. Try to navigate directly to Tenant A's app configuration URL
7. Try to access Tenant A's app through the Apps page

**Expected Results**:
- Tenant B cannot see Tenant A's apps in the Apps page
- Direct URL access to Tenant A's app returns 403 Forbidden or redirects
- Tenant B can only see and configure their own apps
- No data leakage between tenants

---

## Validation Checklist

After completing all tests, verify:

- [ ] Navigation works from Apps page to App Details
- [ ] Configuration tab loads correctly
- [ ] All form fields are present and functional
- [ ] Source selection works (eeh/api)
- [ ] Enrichment APIs can be added, configured, and removed
- [ ] File uploads work for all file types (CSV, JSON, Avro)
- [ ] File validation catches invalid files with specific error messages
- [ ] Request mappings validation works correctly
- [ ] Publisher type switching works (api/eeh)
- [ ] Warehouse publisher configuration works for EEH type
- [ ] Configuration saves successfully
- [ ] Configuration loads correctly after page reload
- [ ] Configuration updates work correctly
- [ ] Validation errors are displayed inline near relevant fields
- [ ] Success messages appear after successful save
- [ ] Multi-tenant isolation is enforced
- [ ] No console errors in browser developer tools
- [ ] No errors in backend logs

## Troubleshooting

### Backend Errors

Check backend logs:
```bash
# View backend terminal output
# Look for any exceptions or error messages
```

### Frontend Errors

1. Open browser Developer Tools (F12)
2. Check Console tab for JavaScript errors
3. Check Network tab for failed API requests
4. Verify API responses contain expected data

### Database Issues

Check database connection:
```bash
docker ps | grep postgres
# Ensure PostgreSQL container is running
```

## Stopping the Servers

When testing is complete:

1. Stop frontend: Press Ctrl+C in frontend terminal
2. Stop backend: Press Ctrl+C in backend terminal
3. Stop database (optional):
   ```bash
   docker-compose down
   ```

## Reporting Issues

If you encounter any issues during testing, please note:
1. Which test scenario failed
2. Exact steps to reproduce
3. Expected vs actual behavior
4. Any error messages (from UI or console)
5. Screenshots if applicable
