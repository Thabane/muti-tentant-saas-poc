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
  create: (data) => api.post('/apps', data),
  getAll: () => api.get('/apps'),
  getById: (id) => api.get(`/apps/${id}`),
  update: (id, data) => api.put(`/apps/${id}`, data),
  delete: (id) => api.delete(`/apps/${id}`),
  regenerateKey: (id) => api.post(`/apps/${id}/regenerate-key`),
  createWorkflow: (appId, data) => api.post('/workflows', { ...data, appId }),
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
