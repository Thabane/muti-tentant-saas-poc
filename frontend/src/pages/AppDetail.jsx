import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { appAPI } from '../services/api';
import ApiKeyDisplay from '../components/ApiKeyDisplay';
import SourceSelector from '../components/SourceSelector';
import EnrichmentApiSection from '../components/EnrichmentApiSection';
import FileUpload from '../components/FileUpload';
import PublisherSection from '../components/PublisherSection';

function AppDetail() {
  const { appId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [app, setApp] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newWorkflow, setNewWorkflow] = useState({ name: '', type: 'BPMN' });
  
  // Configuration state
  const [activeTab, setActiveTab] = useState(location.state?.activeTab || 'workflows');
  const [config, setConfig] = useState(null);
  const [configLoading, setConfigLoading] = useState(false);
  const [configErrors, setConfigErrors] = useState({});
  const [saveSuccess, setSaveSuccess] = useState(false);

  useEffect(() => {
    loadApp();
    if (activeTab === 'configuration') {
      loadConfig();
    }
  }, [appId, activeTab]);

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

  const loadConfig = async () => {
    try {
      setConfigLoading(true);
      const response = await appAPI.getConfig(appId);
      setConfig(response.data);
      setConfigErrors({});
    } catch (err) {
      // Handle 404 - no config exists yet
      if (err.response?.status === 404) {
        setConfig({
          source: '',
          enrichmentApis: [],
          parametersFile: null,
          modelFile: null,
          publisher: null
        });
      } else {
        setConfigErrors({ general: 'Failed to load configuration' });
        console.error('Error loading config:', err);
      }
    } finally {
      setConfigLoading(false);
    }
  };

  const handleSaveConfig = async () => {
    try {
      setSaveSuccess(false);
      setConfigErrors({});
      
      // Validate source is selected
      if (!config.source || config.source.trim() === '') {
        setConfigErrors({ source: 'Please select a data source (EEH or API)' });
        return;
      }
      
      // Transform config data to match backend expectations
      const requestData = {
        source: config.source,
        enrichmentApis: config.enrichmentApis?.map(api => ({
          openApiDocument: typeof api.openApiDocument === 'object' ? api.openApiDocument?.content : api.openApiDocument,
          requestMappings: api.requestMappings,
          configProperties: api.configProperties
        })),
        parametersFile: config.parametersFile ? {
          filename: config.parametersFile.filename,
          content: config.parametersFile.content
        } : null,
        modelFile: config.modelFile ? {
          filename: config.modelFile.filename,
          content: config.modelFile.content
        } : null,
        publisher: config.publisher ? {
          type: config.publisher.type,
          openApiDocument: typeof config.publisher.openApiDocument === 'object' ? 
            config.publisher.openApiDocument?.content : config.publisher.openApiDocument,
          requestMappings: config.publisher.requestMappings,
          configProperties: config.publisher.configProperties,
          warehousePublisher: config.publisher.warehousePublisher ? {
            avroSchema: typeof config.publisher.warehousePublisher.avroSchema === 'object' ?
              config.publisher.warehousePublisher.avroSchema?.content : config.publisher.warehousePublisher.avroSchema,
            mappings: config.publisher.warehousePublisher.mappings
          } : null
        } : null
      };
      
      // Determine if we're creating or updating
      const isUpdate = config.id !== undefined;
      
      if (isUpdate) {
        await appAPI.updateConfig(appId, requestData);
      } else {
        await appAPI.createConfig(appId, requestData);
      }
      
      setSaveSuccess(true);
      await loadConfig(); // Reload to get updated data
      
      // Clear success message after 3 seconds
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      console.error('Error saving config:', err);
      console.error('Error response:', err.response?.data);
      
      if (err.response?.data?.errors) {
        // Backend returns errors in format: { error: "message", errors: { field: "error" } }
        const backendErrors = err.response.data.errors;
        const mainError = err.response.data.error || err.response.data.message;
        
        // Map generic 'file' error to specific file fields for better UX
        const mappedErrors = { ...backendErrors };
        if (backendErrors.file) {
          // Try to determine which file caused the error based on the error message
          const fileError = backendErrors.file;
          if (fileError.includes('CSV')) {
            // Could be parameters or model file - show on both for now
            mappedErrors.parametersFile = fileError;
            mappedErrors.modelFile = fileError;
          } else if (fileError.includes('OpenAPI')) {
            mappedErrors.enrichmentApis = fileError;
          } else if (fileError.includes('Avro')) {
            mappedErrors.publisher = fileError;
          } else {
            // Generic file error - show as general error
            mappedErrors.general = `${mainError}: ${fileError}`;
          }
          delete mappedErrors.file;
        }
        
        setConfigErrors({ 
          general: mainError,
          ...mappedErrors
        });
      } else if (err.response?.data?.error) {
        setConfigErrors({ general: err.response.data.error });
      } else {
        setConfigErrors({ general: err.response?.data?.message || 'Failed to save configuration' });
      }
    }
  };

  const handleSourceChange = (value) => {
    setConfig({ ...config, source: value });
  };

  const handleAddEnrichmentApi = () => {
    setConfig({
      ...config,
      enrichmentApis: [
        ...config.enrichmentApis,
        { openApiDocument: null, requestMappings: '', configProperties: {} }
      ]
    });
  };

  const handleRemoveEnrichmentApi = (index) => {
    const updated = [...config.enrichmentApis];
    updated.splice(index, 1);
    setConfig({ ...config, enrichmentApis: updated });
  };

  const handleUpdateEnrichmentApi = (index, updatedApi) => {
    const updated = [...config.enrichmentApis];
    updated[index] = updatedApi;
    setConfig({ ...config, enrichmentApis: updated });
  };

  const handleParametersUpload = (file) => {
    setConfig({ ...config, parametersFile: file });
  };

  const handleModelUpload = (file) => {
    setConfig({ ...config, modelFile: file });
  };

  const handlePublisherChange = (publisher) => {
    setConfig({ ...config, publisher });
  };

  const regenerateApiKey = async (appId) => {
  try {
    const response = await appAPI.regenerateKey(appId);
    setApp({ ...app, apiKey: response.data.apiKey });
    setError(null);
  } catch (err) {
    const errorMessage = err.response?.data?.error || err.message || 'Failed to regenerate API key';
    setError(errorMessage);
    console.error('Error regenerating API key:', err);
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
        {activeTab === 'workflows' && (
          <button
            onClick={() => {
              setShowCreateModal(true);
              setError(null);
            }}
            className="btn-primary"
          >
            Create Workflow
          </button>
        )}
        {activeTab === 'configuration' && config && (
          <button
            onClick={handleSaveConfig}
            className="btn-primary"
            disabled={configLoading}
          >
            Save Configuration
          </button>
        )}
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {saveSuccess && (
        <div className="alert alert-success">
          Configuration saved successfully!
        </div>
      )}

      {configErrors.general && (
        <div className="alert alert-error">
          {configErrors.general}
        </div>
      )}

      {app.apiKey && (
        <div className="card" style={{ marginBottom: '20px' }}>
          <ApiKeyDisplay 
            apiKey={app.apiKey} 
            appId={app.id} 
            onRegenerate={regenerateApiKey} 
          />
        </div>
      )}

      {/* Tab Navigation */}
      <div className="tab-navigation">
        <button
          onClick={() => setActiveTab('workflows')}
          className={`tab-button ${activeTab === 'workflows' ? 'active' : ''}`}
        >
          Workflows
        </button>
        <button
          onClick={() => setActiveTab('configuration')}
          className={`tab-button ${activeTab === 'configuration' ? 'active' : ''}`}
        >
          Configuration
        </button>
      </div>

      {/* Workflows Tab */}
      {activeTab === 'workflows' && (
        <div className="card">
          <div style={{ marginBottom: '20px' }}>
            <h2>Workflows</h2>
          </div>
          {app.workflows && app.workflows.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
              {app.workflows.map((workflow) => (
                <div key={workflow.id} className="workflow-card">
                  <div>
                    <h3>{workflow.name}</h3>
                    <p style={{ fontSize: '14px', color: '#666', margin: '5px 0' }}>
                      Type: <span className={`workflow-type-badge ${workflow.type.toLowerCase()}`}>
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
      )}

      {/* Configuration Tab */}
      {activeTab === 'configuration' && (
        <div className="card">
          {configLoading ? (
            <p>Loading configuration...</p>
          ) : config ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
              {/* Source Selection */}
              <div className="config-section">
                <h2>Data Source</h2>
                <SourceSelector 
                  value={config.source} 
                  onChange={handleSourceChange}
                  error={configErrors.source}
                />
              </div>

              {/* Enrichment APIs */}
              <div className="config-section">
                <EnrichmentApiSection
                  enrichmentApis={config.enrichmentApis}
                  onAdd={handleAddEnrichmentApi}
                  onRemove={handleRemoveEnrichmentApi}
                  onUpdate={handleUpdateEnrichmentApi}
                  errors={configErrors.enrichmentApis}
                />
              </div>

              {/* Parameters File */}
              <div className="config-section">
                <h2>Parameters File</h2>
                <FileUpload
                  label="Upload Parameters CSV"
                  accept=".csv"
                  file={config.parametersFile}
                  onUpload={handleParametersUpload}
                  error={configErrors.parametersFile}
                />
              </div>

              {/* Model File */}
              <div className="config-section">
                <h2>Model File</h2>
                <FileUpload
                  label="Upload Model CSV"
                  accept=".csv"
                  file={config.modelFile}
                  onUpload={handleModelUpload}
                  error={configErrors.modelFile}
                />
              </div>

              {/* Publisher Configuration */}
              <div className="config-section">
                <PublisherSection
                  publisher={config.publisher}
                  onChange={handlePublisherChange}
                  errors={configErrors.publisher}
                />
              </div>
            </div>
          ) : (
            <p>Failed to load configuration</p>
          )}
        </div>
      )}

      {showCreateModal && (
        <div className="modal-overlay">
          <div className="card modal-content">
            <h2 style={{ marginBottom: '20px' }}>Create New Workflow</h2>

            {error && (
              <div className="alert alert-error">
                {error}
              </div>
            )}

            <form onSubmit={createWorkflow}>
              <div className="form-group">
                <label className="form-label">
                  Workflow Name
                </label>
                <input
                  type="text"
                  value={newWorkflow.name}
                  onChange={(e) => setNewWorkflow({ ...newWorkflow, name: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">
                  Type
                </label>
                <select
                  value={newWorkflow.type}
                  onChange={(e) => setNewWorkflow({ ...newWorkflow, type: e.target.value })}
                >
                  <option value="BPMN">BPMN (Business Process)</option>
                  <option value="DMN">DMN (Decision Table)</option>
                </select>
              </div>
              <div className="modal-actions">
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
