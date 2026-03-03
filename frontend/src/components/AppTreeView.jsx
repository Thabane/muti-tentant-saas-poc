import { useState } from 'react';
import { Link } from 'react-router-dom';

function AppTreeView({ apps }) {
  const [expandedApps, setExpandedApps] = useState(new Set());
  const [expandedWorkflows, setExpandedWorkflows] = useState(new Set());

  const toggleApp = (appId) => {
    const newExpanded = new Set(expandedApps);
    if (newExpanded.has(appId)) {
      newExpanded.delete(appId);
    } else {
      newExpanded.add(appId);
    }
    setExpandedApps(newExpanded);
  };

  const toggleWorkflow = (workflowId) => {
    const newExpanded = new Set(expandedWorkflows);
    if (newExpanded.has(workflowId)) {
      newExpanded.delete(workflowId);
    } else {
      newExpanded.add(workflowId);
    }
    setExpandedWorkflows(newExpanded);
  };

  const renderWorkflow = (workflow, level = 1) => {
    const hasChildren = workflow.children && workflow.children.length > 0;
    const isExpanded = expandedWorkflows.has(workflow.id);
    const indent = level * 20;

    return (
      <div key={workflow.id}>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            padding: '8px 12px',
            paddingLeft: `${indent}px`,
            borderBottom: '1px solid #eee',
            cursor: hasChildren ? 'pointer' : 'default',
            background: level === 1 ? '#f8f9fa' : '#fff',
          }}
          onClick={() => hasChildren && toggleWorkflow(workflow.id)}
        >
          {hasChildren && (
            <span style={{ marginRight: '8px', fontSize: '12px' }}>
              {isExpanded ? '▼' : '▶'}
            </span>
          )}
          {!hasChildren && <span style={{ marginRight: '8px', width: '12px' }}></span>}
          
          <div style={{ flex: 1 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontWeight: level === 1 ? '500' : 'normal' }}>
                {workflow.name}
              </span>
              <span
                style={{
                  padding: '2px 6px',
                  background: workflow.type === 'BPMN' ? '#e3f2fd' : '#f3e5f5',
                  color: workflow.type === 'BPMN' ? '#1976d2' : '#7b1fa2',
                  borderRadius: '3px',
                  fontSize: '11px',
                  fontWeight: 'bold',
                }}
              >
                {workflow.type === 'BPMN' ? 'Service' : workflow.type}
              </span>
            </div>
            {workflow.apiPath && (
              <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                API: <code style={{ background: '#f5f5f5', padding: '2px 4px', borderRadius: '2px' }}>
                  {workflow.apiPath}
                </code>
              </div>
            )}
          </div>
        </div>

        {hasChildren && isExpanded && (
          <div>
            {workflow.children.map((child) => renderWorkflow(child, level + 1))}
          </div>
        )}
      </div>
    );
  };

  if (!apps || apps.length === 0) {
    return (
      <div style={{ padding: '20px', textAlign: 'center', color: '#666' }}>
        No apps available
      </div>
    );
  }

  return (
    <div style={{ border: '1px solid #ddd', borderRadius: '4px', overflow: 'hidden' }}>
      {apps.map((app) => {
        const isExpanded = expandedApps.has(app.id);
        const hasWorkflows = app.workflows && app.workflows.length > 0;

        return (
          <div key={app.id} style={{ borderBottom: '1px solid #ddd' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                padding: '12px 16px',
                background: '#fafafa',
                cursor: 'pointer',
                fontWeight: '600',
              }}
            >
              <span 
                style={{ marginRight: '8px', fontSize: '14px' }}
                onClick={() => toggleApp(app.id)}
              >
                {isExpanded ? '▼' : '▶'}
              </span>
              <span 
                style={{ flex: 1 }}
                onClick={() => toggleApp(app.id)}
              >
                {app.name}
              </span>
              <Link
                to={`/apps/${app.id}`}
                state={{ activeTab: 'configuration' }}
                style={{
                  padding: '4px 8px',
                  marginRight: '12px',
                  background: '#fff',
                  border: '1px solid #ddd',
                  borderRadius: '4px',
                  color: '#333',
                  textDecoration: 'none',
                  fontSize: '12px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  transition: 'all 0.2s',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.background = '#f0f0f0';
                  e.currentTarget.style.borderColor = '#999';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.background = '#fff';
                  e.currentTarget.style.borderColor = '#ddd';
                }}
                onClick={(e) => e.stopPropagation()}
              >
                <span>⚙️</span>
                <span>Config</span>
              </Link>
              <span 
                style={{ fontSize: '12px', color: '#666' }}
                onClick={() => toggleApp(app.id)}
              >
                {hasWorkflows ? `${app.workflows.length} workflow${app.workflows.length !== 1 ? 's' : ''}` : 'No workflows'}
              </span>
            </div>

            {isExpanded && hasWorkflows && (
              <div>
                {app.workflows.map((workflow) => renderWorkflow(workflow))}
              </div>
            )}

            {isExpanded && !hasWorkflows && (
              <div style={{ padding: '20px', textAlign: 'center', color: '#999', fontSize: '14px' }}>
                No workflows in this app
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}

export default AppTreeView;
