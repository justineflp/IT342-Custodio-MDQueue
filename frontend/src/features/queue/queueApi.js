/**
 * API facade for queue and queue entry operations.
 */
import { api } from '../../shared/lib/api'

/* ── Queue operations ── */

export async function getQueuesByClinic(clinicId) {
  try {
    const res = await api.get(`/api/queues/clinic/${clinicId}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getQueue(id) {
  try {
    const res = await api.get(`/api/queues/${id}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function createQueue(clinicId, data) {
  try {
    const res = await api.post(`/api/queues/clinic/${clinicId}`, data)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function updateQueueStatus(queueId, status) {
  try {
    const res = await api.patch(`/api/queues/${queueId}/status`, { status })
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function deleteQueue(queueId) {
  try {
    await api.delete(`/api/queues/${queueId}`)
    return { success: true }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

/* ── Queue Entry operations ── */

export async function joinQueue(queueId) {
  try {
    const res = await api.post(`/api/queue-entries/join/${queueId}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getQueueEntries(queueId) {
  try {
    const res = await api.get(`/api/queue-entries/queue/${queueId}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getWaitingEntries(queueId) {
  try {
    const res = await api.get(`/api/queue-entries/queue/${queueId}/waiting`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getMyEntries() {
  try {
    const res = await api.get('/api/queue-entries/my')
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getMyActiveEntries() {
  try {
    const res = await api.get('/api/queue-entries/my/active')
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function serveNext(queueId) {
  try {
    const res = await api.patch(`/api/queue-entries/serve-next/${queueId}`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function completeEntry(entryId) {
  try {
    const res = await api.patch(`/api/queue-entries/${entryId}/complete`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function cancelEntry(entryId) {
  try {
    const res = await api.patch(`/api/queue-entries/${entryId}/cancel`)
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

export async function getUserProfile() {
  try {
    const res = await api.get('/api/users/me')
    return { success: true, data: res.data.data || res.data }
  } catch (err) {
    return { success: false, message: extractMsg(err) }
  }
}

function extractMsg(err) {
  const data = err && err.response && err.response.data
  return (data && data.message) || 'Something went wrong.'
}
