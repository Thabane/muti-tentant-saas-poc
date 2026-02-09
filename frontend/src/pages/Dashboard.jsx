import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflowAPI, deploymentAPI, tenantAPI } from '../services/api';

function Dashboard() {
  const [workflows, setWorkflows] = useState([]);
  const [deployments, setDeployments] = useState([]);
  const [tenant, setTenant] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [workflowRes, deploymentRes, tenantRes] = await Promise.all([
        workflowAPI.getAll(),
        deploymentAPI.getAll(),
        tenantAPI.getMe()
      ]);
      setWorkflows(workflowRes.data);
      setDeployments(deploymentRes.data);
      setTenant(tenantRes.data);
    } catch (error) {
      console.error('Failed to load data:', error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Dashboard</h1>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <span>{tenant?.name}</span>
          <button className="btn-secondary" onClick={handleLogout}>Logout</button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginBottom: '30px' }}>
        <div className="card">
          <h3>Total Workflows</h3>
          <div style={{ fontSize: '36px', fontWeight: 'bold', marginTop: '10px' }}>
            {workflows.length}
          </div>
        </div>
        <div className="card">
          <h3>Active Deployments</h3>
          <div style={{ fontSize: '36px', fontWeight: 'bold', marginTop: '10px' }}>
            {deployments.filter(d => d.status === 'active').length}
          </div>
        </div>
        <div className="card">
          <h3>Production</h3>
          <div style={{ fontSize: '36px', fontWeight: 'bold', marginTop: '10px' }}>
            {deployments.filter(d => d.environment === 'production').length}
          </div>
        </div>
      </div>

      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2>Workflows</h2>
          <button className="btn-primary" onClick={() => navigate('/workflows')}>
            Create Workflow
          </button>
        </div>
        
        {workflows.length === 0 ? (
          <p>No workflows yet. Create your first workflow to get started.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #ddd' }}>
                <th style={{ padding: '10px', textAlign: 'left' }}>Name</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Type</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Created</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {workflows.map(workflow => (
                <tr key={workflow.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '10px' }}>{workflow.name}</td>
                  <td style={{ padding: '10px' }}>{workflow.type}</td>
                  <td style={{ padding: '10px' }}>{workflow.status}</td>
                  <td style={{ padding: '10px' }}>
                    {new Date(workflow.created_at).toLocaleDateString()}
                  </td>
                  <td style={{ padding: '10px' }}>
                    <button 
                      className="btn-primary" 
                      style={{ padding: '5px 10px', fontSize: '12px' }}
                      onClick={() => navigate(`/workflows/${workflow.id}`)}
                    >
                      Edit
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card" style={{ marginTop: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2>Recent Deployments</h2>
          <button className="btn-primary" onClick={() => navigate('/deployments')}>
            View All
          </button>
        </div>
        
        {deployments.slice(0, 5).map(deployment => (
          <div key={deployment.id} style={{ padding: '10px', borderBottom: '1px solid #eee' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <div>
                <strong>{deployment.workflow_name}</strong>
                <span style={{ marginLeft: '10px', color: '#666' }}>
                  {deployment.environment}
                </span>
              </div>
              <div>
                <span style={{ 
                  padding: '4px 8px', 
                  borderRadius: '4px', 
                  fontSize: '12px',
                  background: deployment.status === 'active' ? '#d4edda' : '#f8d7da',
                  color: deployment.status === 'active' ? '#155724' : '#721c24'
                }}>
                  {deployment.status}
                </span>
                <span style={{ marginLeft: '10px', color: '#666' }}>
                  {deployment.rollout_percentage}% rollout
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Dashboard;
