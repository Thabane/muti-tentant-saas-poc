function FileUpload({ label, accept, file, onUpload, error }) {
  const handleFileChange = async (e) => {
    const selectedFile = e.target.files[0];
    if (!selectedFile) return;

    try {
      const content = await selectedFile.text();
      onUpload({ filename: selectedFile.name, content });
    } catch (err) {
      console.error('Error reading file:', err);
    }
  };

  return (
    <div style={{ marginBottom: '20px' }}>
      <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
        {label}
      </label>
      <input
        type="file"
        accept={accept}
        onChange={handleFileChange}
        style={{
          width: '100%',
          padding: '8px 12px',
          border: '1px solid #ddd',
          borderRadius: '4px',
          fontSize: '14px',
        }}
      />
      {file && file.filename && (
        <div
          style={{
            marginTop: '8px',
            padding: '8px 12px',
            background: '#f0f0f0',
            borderRadius: '4px',
            fontSize: '13px',
            color: '#333',
          }}
        >
          Current file: <strong>{file.filename}</strong>
        </div>
      )}
      {error && (
        <div
          style={{
            marginTop: '8px',
            padding: '8px 12px',
            background: '#f8d7da',
            border: '1px solid #f5c6cb',
            color: '#721c24',
            borderRadius: '4px',
            fontSize: '13px',
          }}
        >
          {error}
        </div>
      )}
    </div>
  );
}

export default FileUpload;
