# App Configuration Error Fix

## Issues Identified

### Issue 1: App Not Found (404)
**Error**: `App not found with id: 1f70e854-9c4b-4bfe-9d2b-cc2eed9861cc`

**Root Cause**: The app ID in the URL doesn't exist in the database. This happens when:
- The app was deleted
- The URL contains an invalid/old app ID
- You're trying to access an app from a different tenant

**Solution**: Navigate to the Apps page and select a valid app from the list.

### Issue 2: JSON Deserialization Error
**Error**: `Cannot deserialize value of type java.lang.String from Object value (token JsonToken.START_OBJECT)`

**Root Cause**: The frontend was sending file data as objects `{filename, content}` for fields that expect strings:
- `openApiDocument` should be a string (JSON content as string)
- `avroSchema` should be a string (JSON content as string)

**Solution**: Transform the data before sending to the backend to extract just the `content` string from file objects.

## Fix Applied

Updated `frontend/src/pages/AppDetail.jsx` - `handleSaveConfig` function to transform data before sending:

```javascript
const requestData = {
  source: config.source,
  enrichmentApis: config.enrichmentApis?.map(api => ({
    openApiDocument: typeof api.openApiDocument === 'object' ? 
      api.openApiDocument?.content : api.openApiDocument,
    requestMappings: api.requestMappings,
    configProperties: api.configProperties
  })),
  parametersFile: config.parametersFile ? {
    filename: config.parametersFile.filename,
    content: config.parametersFile.content
  } : null,
  modelFile: config.modelFile ? {
    filename: config.modelFile.filename,
    content: config.modelFile.content
  } : null,
  publisher: config.publisher ? {
    type: config.publisher.type,
    openApiDocument: typeof config.publisher.openApiDocument === 'object' ? 
      config.publisher.openApiDocument?.content : config.publisher.openApiDocument,
    requestMappings: config.publisher.requestMappings,
    configProperties: config.publisher.configProperties,
    warehousePublisher: config.publisher.warehousePublisher ? {
      avroSchema: typeof config.publisher.warehousePublisher.avroSchema === 'object' ?
        config.publisher.warehousePublisher.avroSchema?.content : 
        config.publisher.warehousePublisher.avroSchema,
      mappings: config.publisher.warehousePublisher.mappings
    } : null
  } : null
};
```

## Testing Steps

1. **Create a new app** (if you don't have one):
   - Go to Apps page
   - Click "Create App"
   - Enter a name and create

2. **Navigate to Configuration**:
   - Click the "Config" button next to the app
   - You should see the Configuration tab

3. **Test configuration creation**:
   - Select a source (eeh or api)
   - Optionally add enrichment APIs with OpenAPI documents
   - Optionally upload parameters/model CSV files
   - Optionally configure publisher
   - Click "Save Configuration"

4. **Verify success**:
   - Success message should appear
   - Configuration should be saved
   - Reload the page to verify data persists

## Backend Status

The backend IS running correctly. The errors you saw were:
1. Expected 404 for non-existent app (working as designed)
2. Expected 404 for non-existent configuration (working as designed)
3. JSON deserialization error (now fixed in frontend)

## Next Steps

1. Restart the frontend to pick up the changes:
   ```bash
   # The frontend dev server should auto-reload
   # If not, restart it manually
   ```

2. Navigate to the Apps page and select a valid app

3. Try creating a configuration with the fixed code
