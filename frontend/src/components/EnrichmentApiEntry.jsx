import FileUpload from './FileUpload';
import KeyValueEditor from './KeyValueEditor';

function EnrichmentApiEntry({ api, onUpdate, onRemove }) {
  const handleOpenApiUpload = (file) => {
    onUpdate({ ...api, openApiDocument: file });
  };

  const handleRequestMappingsChange = (e) => {
    onUpdate({ ...api, requestMappings: e.target.value });
  };

  const handleConfigPropertiesChange = (properties) => {
    onUpdate({ ...api, configProperties: properties });
  };

  return (
    <div
      style={{
        border: '1px solid #ddd',
        borderRadius: '4px',
        padding: '16px',
        marginBottom: '16px',
        background: '#f9f9f9',
      }}
    >
      <FileUpload
        label="OpenAPI Document"
        accept=".json"
        file={api.openApiDocument}
        onUpload={handleOpenApiUpload}
      />

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
          Request Mappings
        </label>
        <textarea
          placeholder="source1 -> target1 | source2 -> target2"
          value={api.requestMappings || ''}
          onChange={handleRequestMappingsChange}
          rows={3}
          style={{
            width: '100%',
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontSize: '14px',
            fontFamily: 'monospace',
            resize: 'vertical',
          }}
        />
        <div
          style={{
            marginTop: '4px',
            fontSize: '12px',
            color: '#666',
          }}
        >
          Format: source -&gt; target | source2 -&gt; target2
        </div>
      </div>

      <KeyValueEditor
        label="Configuration Properties"
        values={api.configProperties || {}}
        onChange={handleConfigPropertiesChange}
      />

      <button
        type="button"
        onClick={onRemove}
        style={{
          padding: '8px 16px',
          background: '#dc3545',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '14px',
          marginTop: '8px',
        }}
      >
        Remove Enrichment API
      </button>
    </div>
  );
}

export default EnrichmentApiEntry;
