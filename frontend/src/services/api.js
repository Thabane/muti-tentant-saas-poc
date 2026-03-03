import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const tenantAPI = {
  register: (data) => api.post('/tenants/register', data),
  login: (data) => api.post('/tenants/login', data),
  getMe: () => api.get('/tenants/me'),
  completeOnboarding: () => api.patch('/tenants/onboarding'),
};

export const appAPI = {
  getAll: () => axios.get('/api/apps'),
  getById: (id) => axios.get(`/api/apps/${id}`),
  create: (data) => axios.post('/api/apps', data),
  update: (id, data) => axios.put(`/api/apps/${id}`, data),
  delete: (id) => axios.delete(`/api/apps/${id}`),
  regenerateKey: (id) => axios.post(`/api/apps/${id}/regenerate-key`),
  createWorkflow: (appId, data) => axios.post('/api/workflows', { ...data, appId }),
  getConfig: (appId) => axios.get(`/api/v1/apps/${appId}/config`),
  createConfig: (appId, config) => axios.post(`/api/v1/apps/${appId}/config`, config),
  updateConfig: (appId, config) => axios.put(`/api/v1/apps/${appId}/config`, config),
  deleteEnrichmentApi: (appId, enrichmentApiId) => axios.delete(`/api/v1/apps/${appId}/config/enrichment-apis/${enrichmentApiId}`),
  deletePublisher: (appId) => axios.delete(`/api/v1/apps/${appId}/config/publisher`),
};

export const workflowAPI = {
  create: (data) => api.post('/workflows', data),
  getAll: () => api.get('/workflows'),
  getById: (id) => api.get(`/workflows/${id}`),
  update: (id, data) => api.put(`/workflows/${id}`, data),
  testRun: (id, inputData) => api.post(`/workflows/${id}/test`, { inputData }),
};

export const deploymentAPI = {
  create: (data) => api.post('/deployments', data),
  getAll: (environment) => api.get('/deployments', { params: { environment } }),
  promote: (id, data) => api.post(`/deployments/${id}/promote`, data),
  updateRollout: (id, percentage) => api.post(`/deployments/${id}/rollout`, { percentage }),
  execute: (id, inputData) => api.post(`/deployments/${id}/execute`, { inputData }),
};

export default api;
