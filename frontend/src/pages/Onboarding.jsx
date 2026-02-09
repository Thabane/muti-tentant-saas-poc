import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tenantAPI } from '../services/api';

function Onboarding() {
  const [step, setStep] = useState(1);
  const navigate = useNavigate();

  const handleComplete = async () => {
    await tenantAPI.completeOnboarding();
    navigate('/dashboard');
  };

  return (
    <div className="container" style={{ maxWidth: '800px', marginTop: '50px' }}>
      <div className="card">
        <h1>Welcome to Workflow SaaS Platform</h1>
        
        {step === 1 && (
          <div style={{ marginTop: '30px' }}>
            <h2>Step 1: Understanding Workflows</h2>
            <p style={{ marginTop: '15px', lineHeight: '1.6' }}>
              Our platform allows you to design custom business processes using BPMN 
              (Business Process Model and Notation) and decision logic using DMN 
              (Decision Model and Notation).
            </p>
            <ul style={{ marginTop: '15px', marginLeft: '20px', lineHeight: '1.8' }}>
              <li>Create visual workflows with drag-and-drop</li>
              <li>Test workflows in a safe environment</li>
              <li>Deploy to non-production for validation</li>
              <li>Promote to production with controlled rollouts</li>
            </ul>
            <button 
              className="btn-primary" 
              style={{ marginTop: '20px' }}
              onClick={() => setStep(2)}
            >
              Next
            </button>
          </div>
        )}
        
        {step === 2 && (
          <div style={{ marginTop: '30px' }}>
            <h2>Step 2: Environment Strategy</h2>
            <p style={{ marginTop: '15px', lineHeight: '1.6' }}>
              We provide three environments for your workflows:
            </p>
            <div style={{ marginTop: '20px' }}>
              <div style={{ marginBottom: '15px' }}>
                <strong>Test:</strong> Simulate and validate workflows without affecting real data
              </div>
              <div style={{ marginBottom: '15px' }}>
                <strong>Non-Production:</strong> Deploy for integration testing and stakeholder review
              </div>
              <div style={{ marginBottom: '15px' }}>
                <strong>Production:</strong> Live environment with selective rollout capabilities
              </div>
            </div>
            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
              <button className="btn-secondary" onClick={() => setStep(1)}>
                Back
              </button>
              <button className="btn-primary" onClick={() => setStep(3)}>
                Next
              </button>
            </div>
          </div>
        )}
        
        {step === 3 && (
          <div style={{ marginTop: '30px' }}>
            <h2>Step 3: Get Started</h2>
            <p style={{ marginTop: '15px', lineHeight: '1.6' }}>
              You're all set! Head to your dashboard to create your first workflow.
            </p>
            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
              <button className="btn-secondary" onClick={() => setStep(2)}>
                Back
              </button>
              <button className="btn-success" onClick={handleComplete}>
                Go to Dashboard
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Onboarding;
