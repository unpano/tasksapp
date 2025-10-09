import axios, { AxiosError } from 'axios'

export const httpClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for debugging
httpClient.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method?.toUpperCase(), config.url, config.params)
    return config
  },
  (error) => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor for debugging
httpClient.interceptors.response.use(
  (response) => {
    console.log('API Response:', response.status, response.config.url)
    return response
  },
  (error) => {
    console.error('Response Error:', error.response?.status, error.response?.data || error.message)
    return Promise.reject(error)
  }
)

export const normalizeError = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string }>
    return axiosError.response?.data?.message || axiosError.message || 'An error occurred'
  }
  if (error instanceof Error) {
    return error.message
  }
  return 'An unknown error occurred'
}
