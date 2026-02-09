import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { deploymentAPI, workflowAPI } from '../services/api';

function Deployments() {
  const [deployments, setDeployments] = useState([]);
  const [workflows, setWorkflows] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const [showExecuteModal, setShowExecuteModal] = useState(false);
  const [selectedDeployment, setSelectedDeployment] = useState(null);
  const [executeInput, setExecuteInput] = useState('{}');
  const [selectedWorkflow, setSelectedWorkflow] = useState('');
  const [environment, setEnvironment] = useState('test');
  const [rollout, setRollout] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [deploymentRes, workflowRes] = await Promise.all([
        deploymentAPI.getAll(),
        workflowAPI.getAll()
      ]);
      setDeployments(deploymentRes.data);
      setWorkflows(workflowRes.data);
    } catch (error) {
      console.error('Failed to load data:', error);
    }
  };

  const handleDeploy = async () => {
    try {
      await deploymentAPI.create({
        workflowId: selectedWorkflow,
        environment,
        rolloutPercentage: rollout
      });
      setShowModal(false);
      loadData();
      alert('Deployment created successfully');
    } catch (error) {
      console.error('Deployment failed:', error);
      alert('Deployment failed');
    }
  };

  const handlePromote = async (deploymentId, currentEnv) => {
    const envMap = {
      'test': 'non-prod',
      'non-prod': 'production'
    };
    
    const targetEnv = envMap[currentEnv];
    if (!targetEnv) {
      alert('Cannot promote from production');
      return;
    }

    const percentage = prompt(`Enter rollout percentage for ${targetEnv} (0-100):`, '100');
    if (percentage === null) return;

    try {
      await deploymentAPI.promote(deploymentId, {
        targetEnvironment: targetEnv,
        rolloutPercentage: parseInt(percentage)
      });
      loadData();
      alert(`Promoted to ${targetEnv} successfully`);
    } catch (error) {
      console.error('Promotion failed:', error);
      alert('Promotion failed');
    }
  };

  const handleRolloutUpdate = async (deploymentId) => {
    const percentage = prompt('Enter new rollout percentage (0-100):');
    if (percentage === null) return;

    try {
      await deploymentAPI.updateRollout(deploymentId, parseInt(percentage));
      loadData();
      alert('Rollout updated successfully');
    } catch (error) {
      console.error('Rollout update failed:', error);
      alert('Rollout update failed');
    }
  };

  const handleExecute = async () => {
    try {
      const inputData = JSON.parse(executeInput);
      await deploymentAPI.execute(selectedDeployment.id, inputData);
      setShowExecuteModal(false);
      alert('Workflow execution started successfully');
    } catch (error) {
      console.error('Execution failed:', error);
      alert('Execution failed: ' + (error.response?.data?.error || error.message));
    }
  };

  const openExecuteModal = (deployment) => {
    setSelectedDeployment(deployment);
    setExecuteInput('{}');
    setShowExecuteModal(true);
  };

  const getEnvColor = (env) => {
    const colors = {
      'test': '#ffc107',
      'non-prod': '#17a2b8',
      'production': '#28a745'
    };
    return colors[env] || '#6c757d';
  };

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>Deployments</h1>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn-secondary" onClick={() => navigate('/dashboard')}>
            Dashboard
          </button>
          <button className="btn-primary" onClick={() => setShowModal(true)}>
            New Deployment
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginBottom: '30px' }}>
        {['test', 'non-prod', 'production'].map(env => (
          <div key={env} className="card">
            <h3 style={{ textTransform: 'capitalize' }}>{env}</h3>
            <div style={{ fontSize: '36px', fontWeight: 'bold', marginTop: '10px', color: getEnvColor(env) }}>
              {deployments.filter(d => d.environment === env && d.status === 'active').length}
            </div>
            <div style={{ fontSize: '14px', color: '#666', marginTop: '5px' }}>
              Active Deployments
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <h2 style={{ marginBottom: '20px' }}>All Deployments</h2>
        
        {deployments.length === 0 ? (
          <p>No deployments yet. Deploy a workflow to get started.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #ddd' }}>
                <th style={{ padding: '10px', textAlign: 'left' }}>Workflow</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Environment</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Rollout</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Deployed</th>
                <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {deployments.map(deployment => (
                <tr key={deployment.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '10px' }}>{deployment.workflow_name}</td>
                  <td style={{ padding: '10px' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: '4px',
                      fontSize: '12px',
                      background: getEnvColor(deployment.environment),
                      color: 'white'
                    }}>
                      {deployment.environment}
                    </span>
                  </td>
                  <td style={{ padding: '10px' }}>{deployment.status}</td>
                  <td style={{ padding: '10px' }}>{deployment.rollout_percentage}%</td>
                  <td style={{ padding: '10px' }}>
                    {new Date(deployment.deployed_at).toLocaleDateString()}
                  </td>
                  <td style={{ padding: '10px' }}>
                    <div style={{ display: 'flex', gap: '5px' }}>
                      {deployment.environment !== 'test' && (
                        <button
                          className="btn-success"
                          style={{ padding: '5px 10px', fontSize: '12px' }}
                          onClick={() => openExecuteModal(deployment)}
                        >
                          Execute
                        </button>
                      )}
                      {deployment.environment !== 'production' && (
                        <button
                          className="btn-primary"
                          style={{ padding: '5px 10px', fontSize: '12px' }}
                          onClick={() => handlePromote(deployment.id, deployment.environment)}
                        >
                          Promote
                        </button>
                      )}
                      <button
                        className="btn-secondary"
                        style={{ padding: '5px 10px', fontSize: '12px' }}
                        onClick={() => handleRolloutUpdate(deployment.id)}
                      >
                        Update Rollout
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}>
          <div className="card" style={{ width: '500px' }}>
            <h2>New Deployment</h2>
            
            <div style={{ marginTop: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px' }}>Workflow</label>
              <select
                value={selectedWorkflow}
                onChange={(e) => setSelectedWorkflow(e.target.value)}
              >
                <option value="">Select a workflow</option>
                {workflows.map(wf => (
                  <option key={wf.id} value={wf.id}>{wf.name}</option>
                ))}
              </select>
            </div>

            <div style={{ marginTop: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px' }}>Environment</label>
              <select
                value={environment}
                onChange={(e) => setEnvironment(e.target.value)}
              >
                <option value="test">Test</option>
                <option value="non-prod">Non-Production</option>
                <option value="production">Production</option>
              </select>
            </div>

            <div style={{ marginTop: '15px' }}>
              <label style={{ display: 'block', marginBottom: '5px' }}>
                Rollout Percentage: {rollout}%
              </label>
              <input
                type="range"
                min="0"
                max="100"
                value={rollout}
                onChange={(e) => setRollout(parseInt(e.target.value))}
                style={{ width: '100%' }}
              />
            </div>

            <div style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setShowModal(false)}>
                Cancel
              </button>
              <button 
                className="btn-primary" 
                onClick={handleDeploy}
                disabled={!selectedWorkflow}
              >
                Deploy
              </button>
            </div>
          </div>
        </div>
      )}

      {showExecuteModal && selectedDeployment && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}>
          <div className="card" style={{ width: '500px' }}>
            <h2>Execute Workflow</h2>
            <p style={{ marginTop: '10px', color: '#666' }}>
              {selectedDeployment.workflow_name} - {selectedDeployment.environment}
            </p>
            
            <div style={{ marginTop: '20px' }}>
              <label style={{ display: 'block', marginBottom: '5px' }}>
                Input Data (JSON)
              </label>
              <textarea
                value={executeInput}
                onChange={(e) => setExecuteInput(e.target.value)}
                placeholder='{"key": "value"}'
                style={{ 
                  minHeight: '150px',
                  fontFamily: 'monospace',
                  fontSize: '12px'
                }}
              />
            </div>

            <div style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button className="btn-secondary" onClick={() => setShowExecuteModal(false)}>
                Cancel
              </button>
              <button className="btn-success" onClick={handleExecute}>
                Execute
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Deployments;
