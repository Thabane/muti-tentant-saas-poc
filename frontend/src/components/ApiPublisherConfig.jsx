import FileUpload from './FileUpload';
import KeyValueEditor from './KeyValueEditor';

function ApiPublisherConfig({ publisher, onChange }) {
  const handleOpenApiUpload = (file) => {
    onChange({ ...publisher, openApiDocument: file });
  };

  const handleRequestMappingsChange = (e) => {
    onChange({ ...publisher, requestMappings: e.target.value });
  };

  const handleConfigPropertiesChange = (properties) => {
    onChange({ ...publisher, configProperties: properties });
  };

  return (
    <div
      style={{
        border: '1px solid #ddd',
        borderRadius: '4px',
        padding: '16px',
        marginTop: '16px',
        background: '#f9f9f9',
      }}
    >
      <FileUpload
        label="OpenAPI Document"
        accept=".json"
        file={publisher?.openApiDocument}
        onUpload={handleOpenApiUpload}
      />

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
          Request Mappings
        </label>
        <textarea
          placeholder="source1 -> target1 | source2 -> target2"
          value={publisher?.requestMappings || ''}
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
        values={publisher?.configProperties || {}}
        onChange={handleConfigPropertiesChange}
      />
    </div>
  );
}

export default ApiPublisherConfig;
