import { apiFetch } from '../../shared/lib/api'

export async function createAppointment(data) {
  return apiFetch('/appointments', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export async function getMyAppointments() {
  return apiFetch('/appointments/me')
}

export async function updateAppointmentStatus(id, status) {
  return apiFetch(`/appointments/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
}

export async function processPayment(appointmentId, paymentMethodId) {
  return apiFetch(`/payments/${appointmentId}/process`, {
    method: 'POST',
    body: JSON.stringify({ paymentMethodId })
  })
}

export async function getDoctors() {
  return apiFetch('/users/doctors')
}

export async function getAllDoctors() {
  return apiFetch('/users/admin/doctors')
}

export async function approveDoctor(id) {
  return apiFetch(`/users/admin/doctors/${id}/approve`, {
    method: 'PATCH'
  })
}

export async function getAllAppointments() {
  return apiFetch('/appointments/all')
}

export async function updateSpecialty(specialty) {
  return apiFetch('/users/me/specialty', {
    method: 'PATCH',
    body: JSON.stringify({ specialty })
  })
}

export async function uploadDocument(appointmentId, file) {
  const formData = new FormData();
  formData.append('file', file);
  return apiFetch(`/appointments/${appointmentId}/documents`, {
    method: 'POST',
    body: formData
  }, true); // Pass true to skip setting Content-Type (fetch handles multipart boundary)
}

export async function getDocuments(appointmentId) {
  return apiFetch(`/appointments/${appointmentId}/documents`)
}

export async function getAppointmentDetails(id) {
  return apiFetch(`/appointments/${id}`)
}

export function getDocumentDownloadUrl(docId) {
  return `/api/appointments/documents/${docId}`
}

export async function getPaymentDetails(appointmentId) {
  return apiFetch(`/payments/${appointmentId}`)
}
