// -----------------------------------------------------------------------------
// File: axiosClient.js
// Purpose: Single axios instance for all calls to the C# Web Service (the
// FAT-service backend). Automatically attaches the JWT and handles 401s by
// logging the user out - keeping the web app as a "thin" UI client, per spec.
// -----------------------------------------------------------------------------
import axios from 'axios'

// Inline comment: point this at your deployed IIS URL in production,
// e.g. "https://your-server/api". Defaults to local dev backend.
export const API_BASE_URL = 'http://localhost:8080/api'


const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('ssm_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('ssm_token')
      localStorage.removeItem('ssm_user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default axiosClient
