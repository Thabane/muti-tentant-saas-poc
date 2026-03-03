import { useState, useEffect } from 'react';
import ApiPublisherConfig from './ApiPublisherConfig';
import EehPublisherConfig from './EehPublisherConfig';

function PublisherSection({ publisher, onChange }) {
  const [type, setType] = useState(publisher?.type || '');

  useEffect(() => {
    setType(publisher?.type || '');
  }, [publisher?.type]);

  const handleTypeChange = (e) => {
    const newType = e.target.value;
    setType(newType);
    
    if (newType === '') {
      onChange(null);
    } else {
      onChange({
        type: newType,
        openApiDocument: newType === 'api' ? publisher?.openApiDocument : undefined,
        requestMappings: newType === 'api' ? publisher?.requestMappings : undefined,
        configProperties: newType === 'api' ? publisher?.configProperties : undefined,
        warehousePublisher: newType === 'eeh' ? publisher?.warehousePublisher : undefined,
      });
    }
  };

  const handleConfigChange = (updatedConfig) => {
    onChange({
      ...publisher,
      ...updatedConfig,
    });
  };

  return (
    <div style={{ marginBottom: '24px' }}>
      <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '16px' }}>
        Publisher Configuration
      </h2>

      <div style={{ marginBottom: '20px' }}>
        <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
          Publisher Type
        </label>
        <select
          value={type}
          onChange={handleTypeChange}
          style={{
            width: '100%',
            padding: '8px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            fontSize: '14px',
            backgroundColor: 'white',
            cursor: 'pointer',
          }}
        >
          <option value="">Select publisher type...</option>
          <option value="api">API</option>
          <option value="eeh">EEH</option>
        </select>
      </div>

      {type === 'api' && (
        <ApiPublisherConfig publisher={publisher} onChange={handleConfigChange} />
      )}

      {type === 'eeh' && (
        <EehPublisherConfig publisher={publisher} onChange={handleConfigChange} />
      )}
    </div>
  );
}

export default PublisherSection;
