import FileUpload from './FileUpload';

function WarehousePublisherConfig({ warehousePublisher, onChange }) {
  const handleAvroSchemaUpload = (file) => {
    onChange({ ...warehousePublisher, avroSchema: file });
  };

  const handleMappingsChange = (e) => {
    onChange({ ...warehousePublisher, mappings: e.target.value });
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
      <h3 style={{ marginTop: 0, marginBottom: '16px', fontSize: '16px' }}>
        Warehouse Publisher Configuration
      </h3>

      <FileUpload
        label="Avro Schema"
        accept=".json,.avsc"
        file={warehousePublisher?.avroSchema}
        onUpload={handleAvroSchemaUpload}
      />

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
          Field Mappings
        </label>
        <textarea
          placeholder="source -> target | source2 -> target2"
          value={warehousePublisher?.mappings || ''}
          onChange={handleMappingsChange}
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
    </div>
  );
}

export default WarehousePublisherConfig;
