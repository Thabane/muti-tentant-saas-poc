# Test Data for Multi-Tenant Workflow SaaS Platform

## 1. Tenant Registration & Authentication

### Register a New Tenant
```bash
curl -X POST http://localhost:3000/api/tenants/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corporation",
    "slug": "acme-corp",
    "email": "admin@acme.com",
    "password": "SecurePass123!"
  }'
```

**Expected Response:**
```json
{
  "tenant": {
    "id": "uuid-here",
    "name": "Acme Corporation",
    "slug": "acme-corp",
    "email": "admin@acme.com",
    "features": {
      "dmn": true,
      "workflows": true
    }
  },
  "token": "jwt-token-here"
}
```

### Login
```bash
curl -X POST http://localhost:3000/api/tenants/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@acme.com",
    "password": "SecurePass123!"
  }'
```

---

## 2. Sample BPMN Workflows

### Simple Approval Process
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                   xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
                   xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                   id="Definitions_1" 
                   targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="ApprovalProcess" name="Simple Approval Process" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="Request Submitted">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    
    <bpmn:userTask id="Task_Review" name="Review Request">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:userTask>
    
    <bpmn:exclusiveGateway id="Gateway_Decision" name="Approved?">
      <bpmn:incoming>Flow_2</bpmn:incoming>
      <bpmn:outgoing>Flow_Approved</bpmn:outgoing>
      <bpmn:outgoing>Flow_Rejected</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    
    <bpmn:endEvent id="End_Approved" name="Request Approved">
      <bpmn:incoming>Flow_Approved</bpmn:incoming>
    </bpmn:endEvent>
    
    <bpmn:endEvent id="End_Rejected" name="Request Rejected">
      <bpmn:incoming>Flow_Rejected</bpmn:incoming>
    </bpmn:endEvent>
    
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_Review" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_Review" targetRef="Gateway_Decision" />
    <bpmn:sequenceFlow id="Flow_Approved" name="Yes" sourceRef="Gateway_Decision" targetRef="End_Approved" />
    <bpmn:sequenceFlow id="Flow_Rejected" name="No" sourceRef="Gateway_Decision" targetRef="End_Rejected" />
  </bpmn:process>
</bpmn:definitions>
```

**Test Input:**
```json
{
  "requestId": "REQ-001",
  "requestor": "John Doe",
  "amount": 5000,
  "description": "New laptop purchase"
}
```

---

### Order Fulfillment Process
```xml
<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
                   xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
                   xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                   id="Definitions_Order" 
                   targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="OrderFulfillment" name="Order Fulfillment" isExecutable="true">
    <bpmn:startEvent id="Start_OrderReceived" name="Order Received">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    
    <bpmn:serviceTask id="Task_CheckInventory" name="Check Inventory">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:serviceTask>
    
    <bpmn:exclusiveGateway id="Gateway_InStock" name="In Stock?">
      <bpmn:incoming>Flow_2</bpmn:incoming>
      <bpmn:outgoing>Flow_InStock</bpmn:outgoing>
      <bpmn:outgoing>Flow_OutOfStock</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    
    <bpmn:serviceTask id="Task_ShipOrder" name="Ship Order">
      <bpmn:incoming>Flow_InStock</bpmn:incoming>
      <bpmn:outgoing>Flow_3</bpmn:outgoing>
    </bpmn:serviceTask>
    
    <bpmn:serviceTask id="Task_NotifyCustomer" name="Notify Customer - Out of Stock">
      <bpmn:incoming>Flow_OutOfStock</bpmn:incoming>
      <bpmn:outgoing>Flow_4</bpmn:outgoing>
    </bpmn:serviceTask>
    
    <bpmn:endEvent id="End_Shipped" name="Order Shipped">
      <bpmn:incoming>Flow_3</bpmn:incoming>
    </bpmn:endEvent>
    
    <bpmn:endEvent id="End_Cancelled" name="Order Cancelled">
      <bpmn:incoming>Flow_4</bpmn:incoming>
    </bpmn:endEvent>
    
    <bpmn:sequenceFlow id="Flow_1" sourceRef="Start_OrderReceived" targetRef="Task_CheckInventory" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_CheckInventory" targetRef="Gateway_InStock" />
    <bpmn:sequenceFlow id="Flow_InStock" name="Yes" sourceRef="Gateway_InStock" targetRef="Task_ShipOrder" />
    <bpmn:sequenceFlow id="Flow_OutOfStock" name="No" sourceRef="Gateway_InStock" targetRef="Task_NotifyCustomer" />
    <bpmn:sequenceFlow id="Flow_3" sourceRef="Task_ShipOrder" targetRef="End_Shipped" />
    <bpmn:sequenceFlow id="Flow_4" sourceRef="Task_NotifyCustomer" targetRef="End_Cancelled" />
  </bpmn:process>
</bpmn:definitions>
```

**Test Input:**
```json
{
  "orderId": "ORD-12345",
  "customerId": "CUST-789",
  "items": [
    {
      "productId": "PROD-001",
      "quantity": 2,
      "price": 29.99
    }
  ],
  "shippingAddress": {
    "street": "123 Main St",
    "city": "San Francisco",
    "state": "CA",
    "zip": "94105"
  }
}
```

---

## 3. Sample DMN Decision Tables

### Credit Score Decision
```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" 
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/" 
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC/" 
             id="CreditDecision" 
             name="Credit Approval Decision" 
             namespace="http://camunda.org/schema/1.0/dmn">
  <decision id="Decision_CreditApproval" name="Credit Approval">
    <decisionTable id="DecisionTable_CreditApproval" hitPolicy="FIRST">
      <input id="Input_CreditScore" label="Credit Score">
        <inputExpression id="InputExpression_1" typeRef="integer">
          <text>creditScore</text>
        </inputExpression>
      </input>
      <input id="Input_Income" label="Annual Income">
        <inputExpression id="InputExpression_2" typeRef="integer">
          <text>annualIncome</text>
        </inputExpression>
      </input>
      <output id="Output_Decision" label="Decision" name="decision" typeRef="string" />
      <output id="Output_Limit" label="Credit Limit" name="creditLimit" typeRef="integer" />
      
      <rule id="Rule_1">
        <inputEntry id="InputEntry_1_1">
          <text>&gt;= 750</text>
        </inputEntry>
        <inputEntry id="InputEntry_1_2">
          <text>&gt;= 50000</text>
        </inputEntry>
        <outputEntry id="OutputEntry_1_1">
          <text>"Approved"</text>
        </outputEntry>
        <outputEntry id="OutputEntry_1_2">
          <text>10000</text>
        </outputEntry>
      </rule>
      
      <rule id="Rule_2">
        <inputEntry id="InputEntry_2_1">
          <text>[650..749]</text>
        </inputEntry>
        <inputEntry id="InputEntry_2_2">
          <text>&gt;= 40000</text>
        </inputEntry>
        <outputEntry id="OutputEntry_2_1">
          <text>"Approved"</text>
        </outputEntry>
        <outputEntry id="OutputEntry_2_2">
          <text>5000</text>
        </outputEntry>
      </rule>
      
      <rule id="Rule_3">
        <inputEntry id="InputEntry_3_1">
          <text>&lt; 650</text>
        </inputEntry>
        <inputEntry id="InputEntry_3_2">
          <text>-</text>
        </inputEntry>
        <outputEntry id="OutputEntry_3_1">
          <text>"Rejected"</text>
        </outputEntry>
        <outputEntry id="OutputEntry_3_2">
          <text>0</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
</definitions>
```

**Test Input:**
```json
{
  "creditScore": 720,
  "annualIncome": 65000,
  "applicantName": "Jane Smith"
}
```

---

### Shipping Cost Calculator
```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/" 
             id="ShippingCost" 
             name="Shipping Cost Calculator" 
             namespace="http://camunda.org/schema/1.0/dmn">
  <decision id="Decision_ShippingCost" name="Calculate Shipping Cost">
    <decisionTable id="DecisionTable_Shipping" hitPolicy="FIRST">
      <input id="Input_Weight" label="Package Weight (kg)">
        <inputExpression id="InputExpression_1" typeRef="double">
          <text>weight</text>
        </inputExpression>
      </input>
      <input id="Input_Distance" label="Distance (km)">
        <inputExpression id="InputExpression_2" typeRef="integer">
          <text>distance</text>
        </inputExpression>
      </input>
      <input id="Input_Priority" label="Priority">
        <inputExpression id="InputExpression_3" typeRef="string">
          <text>priority</text>
        </inputExpression>
      </input>
      <output id="Output_Cost" label="Shipping Cost" name="shippingCost" typeRef="double" />
      <output id="Output_DeliveryDays" label="Delivery Days" name="deliveryDays" typeRef="integer" />
      
      <rule id="Rule_Express">
        <inputEntry><text>-</text></inputEntry>
        <inputEntry><text>-</text></inputEntry>
        <inputEntry><text>"express"</text></inputEntry>
        <outputEntry><text>weight * 5 + distance * 0.5</text></outputEntry>
        <outputEntry><text>1</text></outputEntry>
      </rule>
      
      <rule id="Rule_Standard_Light">
        <inputEntry><text>&lt;= 5</text></inputEntry>
        <inputEntry><text>&lt; 100</text></inputEntry>
        <inputEntry><text>"standard"</text></inputEntry>
        <outputEntry><text>10</text></outputEntry>
        <outputEntry><text>3</text></outputEntry>
      </rule>
      
      <rule id="Rule_Standard_Heavy">
        <inputEntry><text>&gt; 5</text></inputEntry>
        <inputEntry><text>-</text></inputEntry>
        <inputEntry><text>"standard"</text></inputEntry>
        <outputEntry><text>weight * 2 + 15</text></outputEntry>
        <outputEntry><text>5</text></outputEntry>
      </rule>
    </decisionTable>
  </decision>
</definitions>
```

**Test Input:**
```json
{
  "weight": 3.5,
  "distance": 250,
  "priority": "standard"
}
```

---

## 4. API Testing Workflow

### Step 1: Register and Get Token
```bash
# Save the token from registration response
TOKEN="your-jwt-token-here"
```

### Step 2: Create a BPMN Workflow
```bash
curl -X POST http://localhost:3000/api/workflows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Order Processing",
    "type": "bpmn",
    "bpmn_xml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
  }'
```

### Step 3: Create a DMN Workflow
```bash
curl -X POST http://localhost:3000/api/workflows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Credit Decision",
    "type": "dmn",
    "dmn_xml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>..."
  }'
```

### Step 4: List All Workflows
```bash
curl -X GET http://localhost:3000/api/workflows \
  -H "Authorization: Bearer $TOKEN"
```

### Step 5: Test Run a Workflow
```bash
curl -X POST http://localhost:3000/api/workflows/{workflow-id}/test \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "inputData": {
      "orderId": "12345",
      "amount": 100
    }
  }'
```

### Step 6: Create a Deployment
```bash
curl -X POST http://localhost:3000/api/deployments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "workflowId": "workflow-uuid-here",
    "environment": "test",
    "rolloutPercentage": 100
  }'
```

---

## 5. Frontend Testing Guide

### Access the Application
1. Open browser: `http://localhost:5174`
2. Register a new tenant account
3. Complete the onboarding flow

### Create a BPMN Workflow
1. Click "Create Workflow" on Dashboard
2. Select "BPMN (Process)" from the type dropdown
3. Enter workflow name: "Customer Onboarding"
4. Use the visual designer to:
   - Drag a Start Event
   - Add User Tasks
   - Add Gateways for decision points
   - Connect with Sequence Flows
   - Add End Events
5. Click "Save"

### Create a DMN Decision Table
1. Click "Create Workflow" on Dashboard
2. Select "DMN (Decision)" from the type dropdown
3. Enter workflow name: "Discount Calculator"
4. Use the decision table editor to:
   - Define input columns (e.g., orderAmount, customerType)
   - Define output columns (e.g., discountPercentage)
   - Add rules with conditions
5. Click "Save"

### Test a Workflow
1. Open a saved workflow
2. In the right panel, enter test input:
   ```json
   {
     "customerId": "CUST-001",
     "orderAmount": 500,
     "customerType": "premium"
   }
   ```
3. Click "Test Run"
4. View the results in the panel

---

## 6. Sample Test Scenarios

### Scenario 1: Multi-Tenant Isolation
1. Register Tenant A: `admin@companyA.com`
2. Register Tenant B: `admin@companyB.com`
3. Create workflows for both tenants
4. Verify Tenant A cannot see Tenant B's workflows

### Scenario 2: Environment Promotion
1. Create a workflow
2. Deploy to Test environment (100% rollout)
3. Test the deployment
4. Promote to Non-Production (50% rollout)
5. Promote to Production (10% rollout)
6. Gradually increase Production rollout to 100%

### Scenario 3: BPMN + DMN Integration
1. Create a BPMN process with a Business Rule Task
2. Create a DMN decision table
3. Reference the DMN from the BPMN
4. Test the integrated workflow

---

## 7. Common Test Data Sets

### E-commerce Order
```json
{
  "orderId": "ORD-2024-001",
  "customerId": "CUST-12345",
  "orderDate": "2024-02-09T10:00:00Z",
  "items": [
    {"sku": "PROD-001", "quantity": 2, "price": 29.99},
    {"sku": "PROD-002", "quantity": 1, "price": 49.99}
  ],
  "subtotal": 109.97,
  "tax": 9.90,
  "total": 119.87,
  "shippingAddress": {
    "name": "John Doe",
    "street": "123 Main St",
    "city": "San Francisco",
    "state": "CA",
    "zip": "94105",
    "country": "USA"
  }
}
```

### Loan Application
```json
{
  "applicationId": "LOAN-2024-001",
  "applicant": {
    "name": "Jane Smith",
    "ssn": "XXX-XX-1234",
    "dateOfBirth": "1985-05-15",
    "email": "jane.smith@email.com",
    "phone": "+1-555-0123"
  },
  "employment": {
    "employer": "Tech Corp",
    "position": "Software Engineer",
    "yearsEmployed": 5,
    "annualIncome": 95000
  },
  "loanDetails": {
    "amount": 250000,
    "purpose": "home purchase",
    "term": 30
  },
  "creditScore": 740
}
```

### Insurance Claim
```json
{
  "claimId": "CLM-2024-001",
  "policyNumber": "POL-987654",
  "claimant": {
    "name": "Robert Johnson",
    "policyHolder": true,
    "contactNumber": "+1-555-0199"
  },
  "incident": {
    "date": "2024-02-01",
    "type": "auto accident",
    "location": "Highway 101, San Jose, CA",
    "description": "Rear-end collision at traffic light",
    "policeReportNumber": "PD-2024-0201-123"
  },
  "damages": {
    "vehicleDamage": 4500,
    "medicalExpenses": 1200,
    "totalClaim": 5700
  }
}
```

---

## 8. Error Testing

### Invalid Workflow XML
```bash
curl -X POST http://localhost:3000/api/workflows \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Invalid Workflow",
    "type": "bpmn",
    "bpmn_xml": "<invalid>xml</invalid>"
  }'
```

### Unauthorized Access
```bash
curl -X GET http://localhost:3000/api/workflows \
  -H "Authorization: Bearer invalid-token"
```

### Invalid Rollout Percentage
```bash
curl -X POST http://localhost:3000/api/deployments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "workflowId": "workflow-uuid",
    "environment": "production",
    "rolloutPercentage": 150
  }'
```

---

## Quick Start Commands

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Start backend
cd backend && npm run dev

# 3. Start frontend (in another terminal)
cd frontend && npm run dev

# 4. Register a test tenant
curl -X POST http://localhost:3000/api/tenants/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Org","slug":"test","email":"test@test.com","password":"test123"}'

# 5. Open browser
open http://localhost:5174
```
