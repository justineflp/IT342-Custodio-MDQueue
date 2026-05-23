import axios from 'axios'
import { getToken } from '../../features/auth/lib/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
})

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const apiFetch = async (endpoint, options = {}) => {
  try {
    const config = {
      url: `/api${endpoint}`,
      method: options.method || 'GET',
      data: options.body && typeof options.body === 'string' ? JSON.parse(options.body) : options.body
    };

    const res = await api(config)
    return res.data
  } catch (error) {
    if (error.response?.data) {
      return error.response.data
    }
    return { success: false, message: error.message }
  }
}
