function SourceSelector({ value, onChange }) {
  const handleChange = (e) => {
    onChange(e.target.value);
  };

  return (
    <div style={{ marginBottom: '20px' }}>
      <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
        Data Source
      </label>
      <select
        value={value || ''}
        onChange={handleChange}
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
        <option value="">Select source...</option>
        <option value="eeh">EEH</option>
        <option value="api">API</option>
      </select>
    </div>
  );
}

export default SourceSelector;
