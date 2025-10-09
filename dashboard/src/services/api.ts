import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
apiClient.interceptors.request.use(
  config => {
    // Add auth token or other headers here if needed
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// Response interceptor
apiClient.interceptors.response.use(
  response => response,
  error => {
    // Handle errors globally here
    return Promise.reject(error)
  }
)

export default apiClient
