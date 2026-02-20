import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import DmnModeler from 'dmn-js/lib/Modeler';
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule
} from 'bpmn-js-properties-panel';
import CamundaBpmnModdle from 'camunda-bpmn-moddle/resources/camunda.json';
import { workflowAPI } from '../services/api';
import './WorkflowDesigner.css';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import 'dmn-js/dist/assets/diagram-js.css';
import 'dmn-js/dist/assets/dmn-js-decision-table-controls.css';
import 'dmn-js/dist/assets/dmn-js-decision-table.css';
import 'dmn-js/dist/assets/dmn-js-drd.css';
import 'dmn-js/dist/assets/dmn-js-literal-expression.css';
import 'dmn-js/dist/assets/dmn-js-shared.css';
import 'dmn-js/dist/assets/dmn-font/css/dmn-embedded.css';

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

function WorkflowDesigner() {
  const { id } = useParams();
  const navigate = useNavigate();
  const containerRef = useRef(null);
  const propertiesPanelRef = useRef(null);
  const modelerRef = useRef(null);
  
  const [workflow, setWorkflow] = useState(null);
  const [name, setName] = useState('');
  const [workflowType, setWorkflowType] = useState('bpmn'); // 'bpmn' or 'dmn'
  const [testInput, setTestInput] = useState('{}');
  const [testResult, setTestResult] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showProperties, setShowProperties] = useState(true);

  useEffect(() => {
    initializeModeler();

    if (id) {
      loadWorkflow();
    } else {
      setName('New Workflow');
      loadDefaultDiagram();
    }

    return () => {
      modelerRef.current?.destroy();
      modelerRef.current = null;
    };
  }, [id, workflowType]);

  const initializeModeler = () => {
    if (!containerRef.current) return;

    // Destroy existing modeler
    if (modelerRef.current) {
      modelerRef.current.destroy();
      modelerRef.current = null;
    }

    // Create new modeler based on type
    if (workflowType === 'bpmn') {
      modelerRef.current = new BpmnModeler({
        container: containerRef.current,
        keyboard: { bindTo: document },
        propertiesPanel: {
          parent: propertiesPanelRef.current
        },
        additionalModules: [
          BpmnPropertiesPanelModule,
          BpmnPropertiesProviderModule,
          CamundaPlatformPropertiesProviderModule
        ],
        moddleExtensions: {
          camunda: CamundaBpmnModdle
        }
      });
    } else {
      modelerRef.current = new DmnModeler({
        container: containerRef.current,
        keyboard: { bindTo: document }
      });
    }
  };

  const loadDefaultDiagram = async () => {
    try {
      const xml = workflowType === 'bpmn' ? defaultBpmn : defaultDmn;
      await modelerRef.current?.importXML(xml);
    } catch (error) {
      console.error('Failed to load default diagram:', error);
    }
  };

  const loadWorkflow = async () => {
    try {
      setIsLoading(true);
      const response = await workflowAPI.getById(id);
      const wf = response.data;
      setWorkflow(wf);
      setName(wf.name);
      // Convert type to lowercase for modeler initialization
      const type = wf.type.toLowerCase();
      setWorkflowType(type);
      
      // Wait for modeler to be initialized with correct type
      await new Promise(resolve => setTimeout(resolve, 100));
      
      const xml = wf.bpmnXml || wf.bpmn_xml;
      if (xml && modelerRef.current) {
        await modelerRef.current.importXML(xml);
      }
    } catch (error) {
      console.error('Failed to load workflow:', error);
      alert('Failed to load workflow');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    try {
      setIsLoading(true);
      
      if (!modelerRef.current) {
        throw new Error('Modeler not initialized');
      }
      
      const { xml } = await modelerRef.current.saveXML({ format: true });
      
      if (!xml) {
        throw new Error('Failed to generate XML from diagram');
      }
      
      const payload = {
        name,
        type: workflowType.toUpperCase(), // Convert to uppercase for backend
        bpmnXml: xml // Always use bpmnXml field (backend stores both BPMN and DMN in this field)
      };

      if (id) {
        await workflowAPI.update(id, payload);
        alert('Workflow updated successfully');
      } else {
        const response = await workflowAPI.create(payload);
        navigate(`/workflows/${response.data.id}`);
      }
    } catch (error) {
      console.error('Failed to save workflow:', error);
      alert(`Failed to save workflow: ${error.response?.data?.error || error.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTest = async () => {
    if (!id) {
      alert('Please save the workflow first');
      return;
    }

    try {
      const inputData = JSON.parse(testInput);
      const response = await workflowAPI.testRun(id, inputData);
      setTestResult(response.data);
    } catch (error) {
      console.error('Test failed:', error);
      alert('Test execution failed');
    }
  };

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column' }}>
      <div style={{ 
        background: 'white', 
        padding: '15px 20px', 
        borderBottom: '1px solid #ddd',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <button className="btn-secondary" onClick={() => navigate('/dashboard')}>
            ← Back
          </button>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            style={{ fontSize: '18px', fontWeight: 'bold', width: '300px' }}
            disabled={isLoading}
          />
          {!id && (
            <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
              <label style={{ fontSize: '14px', fontWeight: 'normal' }}>Type:</label>
              <select 
                value={workflowType} 
                onChange={(e) => setWorkflowType(e.target.value)}
                style={{ padding: '5px 10px', fontSize: '14px' }}
              >
                <option value="bpmn">BPMN (Process)</option>
                <option value="dmn">DMN (Decision)</option>
              </select>
            </div>
          )}
          {id && (
            <span style={{ 
              fontSize: '12px', 
              padding: '4px 8px', 
              background: workflowType === 'bpmn' ? '#e3f2fd' : '#f3e5f5',
              color: workflowType === 'bpmn' ? '#1976d2' : '#7b1fa2',
              borderRadius: '4px',
              fontWeight: 'bold'
            }}>
              {workflowType.toUpperCase()}
            </span>
          )}
        </div>
        <div style={{ display: 'flex', gap: '10px' }}>
          {workflowType === 'bpmn' && (
            <button 
              className="btn-secondary" 
              onClick={() => setShowProperties(!showProperties)}
            >
              {showProperties ? 'Hide' : 'Show'} Properties
            </button>
          )}
          <button 
            className="btn-primary" 
            onClick={handleTest}
            disabled={isLoading || !id}
          >
            Test Run
          </button>
          <button 
            className="btn-success" 
            onClick={handleSave}
            disabled={isLoading}
          >
            {isLoading ? 'Saving...' : 'Save'}
          </button>
        </div>
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        <div style={{ display: 'flex', flex: 1, position: 'relative' }}>
          <div 
            ref={containerRef} 
            style={{ flex: 1, background: '#fafafa' }}
          />
          
          {workflowType === 'bpmn' && showProperties && (
            <div 
              ref={propertiesPanelRef}
              style={{ 
                width: '300px', 
                background: 'white', 
                borderLeft: '1px solid #ddd',
                overflowY: 'auto',
                height: '100%'
              }}
            />
          )}
        </div>
        
        <div style={{ 
          width: '350px', 
          background: 'white', 
          borderLeft: '1px solid #ddd',
          padding: '20px',
          overflowY: 'auto'
        }}>
          <h3>Test Workflow</h3>
          <p style={{ fontSize: '14px', color: '#666', marginTop: '10px' }}>
            {workflowType === 'bpmn' 
              ? 'Provide input data in JSON format to test your process workflow.'
              : 'Provide input data in JSON format to test your decision logic.'}
          </p>
          
          <textarea
            value={testInput}
            onChange={(e) => setTestInput(e.target.value)}
            placeholder={workflowType === 'bpmn' 
              ? '{"orderId": "12345", "amount": 100}'
              : '{"input": "value"}'}
            style={{ 
              marginTop: '15px', 
              minHeight: '150px',
              fontFamily: 'monospace',
              fontSize: '12px'
            }}
            disabled={!id}
          />

          {!id && (
            <p style={{ fontSize: '12px', color: '#999', marginTop: '10px', fontStyle: 'italic' }}>
              Save the workflow first to enable testing
            </p>
          )}

          {testResult && (
            <div style={{ marginTop: '20px' }}>
              <h4>Test Result</h4>
              <div style={{ 
                marginTop: '10px',
                padding: '10px',
                background: testResult.status === 'completed' ? '#e8f5e9' : '#ffebee',
                borderRadius: '4px',
                fontSize: '12px',
                fontFamily: 'monospace'
              }}>
                <div>
                  <strong>Status:</strong> 
                  <span style={{ 
                    marginLeft: '8px',
                    color: testResult.status === 'completed' ? '#2e7d32' : '#c62828',
                    fontWeight: 'bold'
                  }}>
                    {testResult.status}
                  </span>
                </div>
                <div style={{ marginTop: '10px' }}>
                  <strong>Output:</strong>
                  <pre style={{ 
                    marginTop: '5px', 
                    whiteSpace: 'pre-wrap',
                    background: 'white',
                    padding: '8px',
                    borderRadius: '4px'
                  }}>
                    {JSON.stringify(testResult.output, null, 2)}
                  </pre>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default WorkflowDesigner;
