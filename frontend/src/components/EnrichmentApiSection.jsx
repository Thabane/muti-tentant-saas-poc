import EnrichmentApiEntry from './EnrichmentApiEntry';

function EnrichmentApiSection({ enrichmentApis, onAdd, onRemove, onUpdate }) {
  return (
    <div style={{ marginBottom: '32px' }}>
      <h2 style={{ fontSize: '18px', fontWeight: '600', marginBottom: '16px' }}>
        Enrichment APIs
      </h2>

      {enrichmentApis && enrichmentApis.length > 0 ? (
        enrichmentApis.map((api, index) => (
          <EnrichmentApiEntry
            key={api.id || index}
            api={api}
            onUpdate={(updated) => onUpdate(index, updated)}
            onRemove={() => onRemove(index)}
          />
        ))
      ) : (
        <div
          style={{
            padding: '16px',
            background: '#f5f5f5',
            borderRadius: '4px',
            color: '#666',
            fontSize: '14px',
            marginBottom: '16px',
          }}
        >
          No enrichment APIs configured. Click "Add Enrichment API" to add one.
        </div>
      )}

      <button
        type="button"
        onClick={onAdd}
        style={{
          padding: '10px 20px',
          background: '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '14px',
          fontWeight: '500',
        }}
      >
        Add Enrichment API
      </button>
    </div>
  );
}

export default EnrichmentApiSection;
