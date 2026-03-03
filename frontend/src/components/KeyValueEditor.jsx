import { useState, useEffect } from 'react';

function KeyValueEditor({ label, values, onChange }) {
  // Convert Map or object to array of {key, value} pairs
  const [pairs, setPairs] = useState([]);

  useEffect(() => {
    if (!values) {
      setPairs([{ key: '', value: '' }]);
      return;
    }

    // Handle both Map and plain object
    const entries = values instanceof Map 
      ? Array.from(values.entries())
      : Object.entries(values);

    if (entries.length === 0) {
      setPairs([{ key: '', value: '' }]);
    } else {
      setPairs(entries.map(([key, value]) => ({ key, value })));
    }
  }, [values]);

  const handlePairChange = (index, field, newValue) => {
    const updatedPairs = [...pairs];
    updatedPairs[index][field] = newValue;
    setPairs(updatedPairs);

    // Convert back to object and call onChange
    const newValues = {};
    updatedPairs.forEach(pair => {
      if (pair.key.trim()) {
        newValues[pair.key.trim()] = pair.value;
      }
    });
    onChange(newValues);
  };

  const handleAddPair = () => {
    setPairs([...pairs, { key: '', value: '' }]);
  };

  const handleRemovePair = (index) => {
    if (pairs.length === 1) {
      // Keep at least one empty pair
      setPairs([{ key: '', value: '' }]);
      onChange({});
      return;
    }

    const updatedPairs = pairs.filter((_, i) => i !== index);
    setPairs(updatedPairs);

    // Convert back to object and call onChange
    const newValues = {};
    updatedPairs.forEach(pair => {
      if (pair.key.trim()) {
        newValues[pair.key.trim()] = pair.value;
      }
    });
    onChange(newValues);
  };

  return (
    <div style={{ marginBottom: '20px' }}>
      <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
        {label}
      </label>
      <div style={{ border: '1px solid #ddd', borderRadius: '4px', padding: '12px' }}>
        {pairs.map((pair, index) => (
          <div
            key={index}
            style={{
              display: 'flex',
              gap: '8px',
              marginBottom: '8px',
              alignItems: 'center',
            }}
          >
            <input
              type="text"
              placeholder="Key"
              value={pair.key}
              onChange={(e) => handlePairChange(index, 'key', e.target.value)}
              style={{
                flex: '1',
                padding: '8px 12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
              }}
            />
            <input
              type="text"
              placeholder="Value"
              value={pair.value}
              onChange={(e) => handlePairChange(index, 'value', e.target.value)}
              style={{
                flex: '1',
                padding: '8px 12px',
                border: '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
              }}
            />
            <button
              type="button"
              onClick={() => handleRemovePair(index)}
              style={{
                padding: '8px 12px',
                background: '#dc3545',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '14px',
                minWidth: '70px',
              }}
            >
              Remove
            </button>
          </div>
        ))}
        <button
          type="button"
          onClick={handleAddPair}
          style={{
            padding: '8px 16px',
            background: '#28a745',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '14px',
            marginTop: '4px',
          }}
        >
          Add Property
        </button>
      </div>
    </div>
  );
}

export default KeyValueEditor;
