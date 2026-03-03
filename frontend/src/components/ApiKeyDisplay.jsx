import { useState } from 'react';

function ApiKeyDisplay({ apiKey, appId, onRegenerate }) {
  const [isVisible, setIsVisible] = useState(false);
  const [copySuccess, setCopySuccess] = useState(false);
  const [isRegenerating, setIsRegenerating] = useState(false);

  const toggleVisibility = () => {
    setIsVisible(!isVisible);
  };

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(apiKey);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

  const handleRegenerate = async () => {
    if (!window.confirm('Are you sure you want to regenerate the API key? The old key will stop working immediately.')) {
      return;
    }

    setIsRegenerating(true);
    try {
      await onRegenerate(appId);
    } finally {
      setIsRegenerating(false);
    }
  };

  const maskedKey = apiKey ? `${apiKey.substring(0, 8)}${'*'.repeat(24)}` : '';

  return (
    <div style={{ marginTop: '20px' }}>
      <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
        API Key
      </label>
      <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
        <div
          style={{
            flex: 1,
            padding: '10px 12px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            background: '#f8f9fa',
            fontFamily: 'monospace',
            fontSize: '14px',
          }}
        >
          {isVisible ? apiKey : maskedKey}
        </div>
        <button
          onClick={toggleVisibility}
          style={{
            padding: '10px 16px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            background: '#fff',
            cursor: 'pointer',
          }}
          title={isVisible ? 'Hide' : 'Show'}
        >
          {isVisible ? '👁️' : '👁️‍🗨️'}
        </button>
        <button
          onClick={copyToClipboard}
          style={{
            padding: '10px 16px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            background: copySuccess ? '#d4edda' : '#fff',
            cursor: 'pointer',
          }}
          title="Copy to clipboard"
        >
          {copySuccess ? '✓' : '📋'}
        </button>
        <button
          onClick={handleRegenerate}
          disabled={isRegenerating}
          style={{
            padding: '10px 16px',
            border: '1px solid #ddd',
            borderRadius: '4px',
            background: '#fff',
            cursor: isRegenerating ? 'not-allowed' : 'pointer',
            opacity: isRegenerating ? 0.6 : 1,
          }}
          title="Regenerate API key"
        >
          {isRegenerating ? '⏳' : '🔄'}
        </button>
      </div>
      <div style={{ fontSize: '12px', color: '#666', marginTop: '8px' }}>
        Keep this key secure. It provides access to your app's API endpoints.
      </div>
    </div>
  );
}

export default ApiKeyDisplay;
