import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { appAPI } from '../services/api';

function Apps() {
  const [apps, setApps] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newAppName, setNewAppName] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadApps();
  }, []);

  const loadApps = async () => {
    try {
      setLoading(true);
      const response = await appAPI.getAll();
      setApps(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load apps');
      console.error('Error loading apps:', err);
    } finally {
      setLoading(false);
    }
  };

  const createApp = async (e) => {
    e.preventDefault();
    try {
      console.log('Creating app with name:', newAppName);
      console.log('Request payload:', { name: newAppName });
      const response = await appAPI.create({ name: newAppName });
      console.log('App created successfully:', response.data);
      setNewAppName('');
      setShowCreateModal(false);
      setError(null); // Clear any previous errors
      loadApps();
    } catch (err) {
      console.error('Error creating app:', err);
      console.error('Error response:', err.response);
      console.error('Error response data:', err.response?.data);
      console.error('Error message from backend:', err.response?.data?.error);
      const errorMessage = err.response?.data?.error || err.message || 'Failed to create app';
      setError(errorMessage);
      // Don't close modal on error so user can see the error message
    }
  };

  const deleteApp = async (appId) => {
    if (!window.confirm('Are you sure you want to delete this app?')) {
      return;
    }
    try {
      await appAPI.delete(appId);
      loadApps();
    } catch (err) {
      setError('Failed to delete app');
      console.error('Error deleting app:', err);
    }
  };

  if (loading) {
    return (
      <div className="container">
        <p>Loading apps...</p>
      </div>
    );
  }

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <button 
            className="btn-secondary" 
            onClick={() => navigate('/dashboard')}
            style={{ padding: '8px 16px' }}
          >
            ← Back
          </button>
          <h1>Apps</h1>
        </div>
        <button
          onClick={() => {
            setShowCreateModal(true);
            setError(null);
          }}
          className="btn-primary"
        >
          Create App
        </button>
      </div>

      {error && (
        <div style={{ 
          background: '#f8d7da', 
          border: '1px solid #f5c6cb', 
          color: '#721c24', 
          padding: '12px 16px', 
          borderRadius: '4px', 
          marginBottom: '20px' 
        }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
        {apps.map((app) => (
          <div key={app.id} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '15px' }}>
              <h3>{app.name}</h3>
              <button
                onClick={() => deleteApp(app.id)}
                style={{ 
                  color: '#dc3545', 
                  background: 'none', 
                  border: 'none', 
                  cursor: 'pointer',
                  fontSize: '14px'
                }}
              >
                Delete
              </button>
            </div>
            <div style={{ fontSize: '14px', color: '#666', marginBottom: '15px' }}>
              <p>Created: {new Date(app.createdAt).toLocaleDateString()}</p>
              <p style={{ marginTop: '8px' }}>Workflows: {app.workflows?.length || 0}</p>
            </div>
            <button
              onClick={() => navigate(`/apps/${app.id}`)}
              className="btn-primary"
              style={{ width: '100%' }}
            >
              View Details
            </button>
          </div>
        ))}
      </div>

      {apps.length === 0 && (
        <div className="card" style={{ textAlign: 'center', padding: '40px' }}>
          <p style={{ color: '#666' }}>No apps yet. Create your first app to get started.</p>
        </div>
      )}

      {showCreateModal && (
        <div style={{ 
          position: 'fixed', 
          inset: 0, 
          background: 'rgba(0, 0, 0, 0.5)', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div className="card" style={{ maxWidth: '500px', width: '90%' }}>
            <h2 style={{ marginBottom: '20px' }}>Create New App</h2>
            
            {error && (
              <div style={{ 
                background: '#f8d7da', 
                border: '1px solid #f5c6cb', 
                color: '#721c24', 
                padding: '12px 16px', 
                borderRadius: '4px', 
                marginBottom: '20px' 
              }}>
                {error}
              </div>
            )}
            
            <form onSubmit={createApp}>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
                  App Name
                </label>
                <input
                  type="text"
                  value={newAppName}
                  onChange={(e) => setNewAppName(e.target.value)}
                  style={{ 
                    width: '100%', 
                    padding: '8px 12px', 
                    border: '1px solid #ddd', 
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                  required
                />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateModal(false);
                    setError(null);
                  }}
                  className="btn-secondary"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="btn-primary"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Apps;
