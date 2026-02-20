import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { appAPI } from '../services/api';

function AppDetail() {
  const { appId } = useParams();
  const navigate = useNavigate();
  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newWorkflow, setNewWorkflow] = useState({ name: '', type: 'BPMN' });

  useEffect(() => {
    loadApp();
  }, [appId]);

  const loadApp = async () => {
    try {
      setLoading(true);
      const response = await appAPI.getById(appId);
      setApp(response.data);
      setError(null);
    } catch (err) {
      setError('Failed to load app details');
      console.error('Error loading app:', err);
    } finally {
      setLoading(false);
    }
  };

  const createWorkflow = async (e) => {
    e.preventDefault();
    try {
      console.log('Creating workflow:', newWorkflow);
      
      // Default BPMN/DMN XML templates
      const defaultBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                   xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                   xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                   id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true" camunda:historyTimeToLive="180">
    <bpmn:startEvent id="StartEvent_1"/>
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36"/>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

      const defaultDmn = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" 
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/" 
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC/" 
             id="Definitions_1" name="DRD" namespace="http://camunda.org/schema/1.0/dmn">
  <decision id="Decision_1" name="Decision 1">
    <decisionTable id="DecisionTable_1">
      <input id="Input_1" label="Input">
        <inputExpression id="InputExpression_1" typeRef="string">
          <text>input</text>
        </inputExpression>
      </input>
      <output id="Output_1" label="Output" name="output" typeRef="string" />
    </decisionTable>
  </decision>
</definitions>`;
      
      // Create workflow with app association and default XML
      const response = await appAPI.createWorkflow(appId, {
        name: newWorkflow.name,
        type: newWorkflow.type,
        appId: appId,
        bpmnXml: newWorkflow.type === 'BPMN' ? defaultBpmn : null,
        dmnXml: newWorkflow.type === 'DMN' ? defaultDmn : null
      });
      
      console.log('Workflow created successfully:', response.data);
      
      // Navigate to workflow designer to edit the BPMN/DMN
      navigate(`/workflows/${response.data.id}`);
    } catch (err) {
      const errorMessage = err.response?.data?.error || err.message || 'Failed to create workflow';
      setError(errorMessage);
      console.error('Error creating workflow:', err);
    }
  };

  const openWorkflowDesigner = (workflowId) => {
    navigate(`/workflows/${workflowId}`);
  };

  if (loading) {
    return (
      <div className="container">
        <p>Loading app details...</p>
      </div>
    );
  }

  if (!app) {
    return (
      <div className="container">
        <p>App not found</p>
      </div>
    );
  }

  return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <button 
            className="btn-secondary" 
            onClick={() => navigate('/apps')}
            style={{ padding: '8px 16px' }}
          >
            ← Back
          </button>
          <h1>{app.name}</h1>
        </div>
        <button
          onClick={() => {
            setShowCreateModal(true);
            setError(null);
          }}
          className="btn-primary"
        >
          Create Workflow
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

      <div className="card">
        <div style={{ marginBottom: '20px' }}>
          <h2>Workflows</h2>
        </div>
        {app.workflows && app.workflows.length > 0 ? (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
            {app.workflows.map((workflow) => (
              <div
                key={workflow.id}
                style={{ 
                  border: '1px solid #ddd', 
                  borderRadius: '4px', 
                  padding: '15px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  background: '#fafafa'
                }}
              >
                <div>
                  <h3 style={{ marginBottom: '5px' }}>{workflow.name}</h3>
                  <p style={{ fontSize: '14px', color: '#666', margin: '5px 0' }}>
                    Type: <span style={{ 
                      padding: '2px 8px', 
                      background: workflow.type === 'BPMN' ? '#e3f2fd' : '#f3e5f5',
                      color: workflow.type === 'BPMN' ? '#1976d2' : '#7b1fa2',
                      borderRadius: '4px',
                      fontSize: '12px',
                      fontWeight: 'bold'
                    }}>
                      {workflow.type}
                    </span>
                  </p>
                  {workflow.description && (
                    <p style={{ fontSize: '14px', color: '#666', marginTop: '5px' }}>{workflow.description}</p>
                  )}
                </div>
                <button
                  onClick={() => openWorkflowDesigner(workflow.id)}
                  className="btn-primary"
                >
                  Edit
                </button>
              </div>
            ))}
          </div>
        ) : (
          <p style={{ color: '#666' }}>No workflows yet. Create your first workflow to get started.</p>
        )}
      </div>

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
            <h2 style={{ marginBottom: '20px' }}>Create New Workflow</h2>

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

            <form onSubmit={createWorkflow}>
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
                  Workflow Name
                </label>
                <input
                  type="text"
                  value={newWorkflow.name}
                  onChange={(e) => setNewWorkflow({ ...newWorkflow, name: e.target.value })}
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
              <div style={{ marginBottom: '20px' }}>
                <label style={{ display: 'block', marginBottom: '8px', fontWeight: '500' }}>
                  Type
                </label>
                <select
                  value={newWorkflow.type}
                  onChange={(e) => setNewWorkflow({ ...newWorkflow, type: e.target.value })}
                  style={{ 
                    width: '100%', 
                    padding: '8px 12px', 
                    border: '1px solid #ddd', 
                    borderRadius: '4px',
                    fontSize: '14px'
                  }}
                >
                  <option value="BPMN">BPMN (Business Process)</option>
                  <option value="DMN">DMN (Decision Table)</option>
                </select>
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
                  Create & Design
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default AppDetail;
