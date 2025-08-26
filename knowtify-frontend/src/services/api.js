// src/services/api.js
import axios from 'axios'

// Base URL for the backend API
const API_BASE_URL = 'http://localhost:8080/api/v1'

// Create a single axios instance for the whole app
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach JWT from localStorage on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Optional response interceptor to normalize errors (kept simple)
api.interceptors.response.use(
  (res) => res,
  (err) => Promise.reject(err)
)

// Auth endpoints (named export)
export const authAPI = {
  register: (data) => api.post('/users/register', data),
  login: (data) => api.post('/users/login', data),
}

// Study/knowledge endpoints (named export)
export const studyAPI = {
  createEntry: (data) => api.post('/entries', data),
  getWeeklyReport: (params) => api.get('/reports/weekly', { params }),
  getKnowledgeMap: (params) => api.get('/knowledge-map', { params }),
  listEntries: (limit = 10) => api.get('/entries', { params: { limit } }), // NEW
}


// Health check (named export)
export const healthCheck = () => api.get('/health')

// Default export of the axios instance (optional to import if needed)
export default api
