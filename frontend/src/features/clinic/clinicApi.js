/**
 * API facade for clinic operations.
 */
import { api } from '../../shared/lib/api'

export async function getClinics(search) {
  try {
    const params = search ? { search } : {}
    const res = await api.get('/api/clinics', { params })
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getClinic(id) {
  try {
    const res = await api.get(`/api/clinics/${id}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function createClinic(data) {
  try {
    const res = await api.post('/api/clinics', data)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function updateClinic(id, data) {
  try {
    const res = await api.put(`/api/clinics/${id}`, data)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function deleteClinic(id) {
  try {
    await api.delete(`/api/clinics/${id}`)
    return { success: true }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getMyClinics() {
  try {
    const res = await api.get('/api/clinics/mine')
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

function extractMsg(err) {
  const data = err && err.response && err.response.data
  return (data && data.message) || 'Something went wrong.'
}
