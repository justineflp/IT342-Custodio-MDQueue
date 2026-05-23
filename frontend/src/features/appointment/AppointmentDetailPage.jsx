import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getMyAppointments, updateAppointmentStatus, processPayment, uploadDocument, getDocuments, getDocumentDownloadUrl } from './appointmentApi'
import { apiFetch } from '../../shared/lib/api'

export default function AppointmentDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [appointment, setAppointment] = useState(null)
  const [loading, setLoading] = useState(true)
  const [role, setRole] = useState('PATIENT')
  const [paying, setPaying] = useState(false)
  const [updating, setUpdating] = useState(false)
  const [error, setError] = useState('')
  const [documents, setDocuments] = useState([])
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState('')

  useEffect(() => {
    async function load() {
      setLoading(true)
      const userRes = await apiFetch('/users/me')
      if (userRes.success) setRole(userRes.data.role)

      const apptRes = await getMyAppointments()
      if (apptRes.success) {
        const found = apptRes.data.find(a => a.id.toString() === id)
        setAppointment(found || null)
        
        // Fetch documents
        if (found) {
          const docRes = await getDocuments(found.id)
          if (docRes.success) {
            setDocuments(docRes.data || [])
          }
        }
      }
      setLoading(false)
    }
    load()
  }, [id])

  const handleStatusUpdate = async (status) => {
    setUpdating(true)
    try {
      const res = await updateAppointmentStatus(id, status)
      if (res.success) {
        setAppointment(res.data)
      } else {
        setError(res.message || 'Failed to update status')
      }
    } catch (err) {
      setError('An error occurred')
    }
    setUpdating(false)
  }

  const handlePayment = async () => {
    setPaying(true)
    setError('')
    try {
      // simulate sandbox token
      const token = 'tok_sandbox_success_' + Date.now()
      const res = await processPayment(id, token)
      if (res.success) {
        // reload appointment to see confirmed status
        const apptRes = await getMyAppointments()
        if (apptRes.success) {
          const found = apptRes.data.find(a => a.id.toString() === id)
          setAppointment(found || null)
        }
        alert('Payment successful! Appointment confirmed.')
      } else {
        setError('Payment failed. Please try again.')
      }
    } catch (err) {
      setError('An error occurred during payment.')
    }
    setPaying(false)
  }

  const handleFileUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    
    setUploading(true)
    setUploadError('')
    try {
      const res = await uploadDocument(id, file)
      if (res.success) {
        setDocuments([res.data, ...documents])
      } else {
        setUploadError(res.message || 'Failed to upload document')
      }
    } catch (err) {
      setUploadError('An error occurred while uploading.')
    }
    setUploading(false)
    e.target.value = null // reset input
  }

  if (loading) return <AppLayout><div className="loading-card">Loading details...</div></AppLayout>
  if (!appointment) return <AppLayout><div className="dash-page"><p>Appointment not found.</p></div></AppLayout>

  return (
    <AppLayout>
      <div className="dash-page" style={{ maxWidth: '800px', margin: '0 auto' }}>
        <button className="secondaryBtn small-btn" onClick={() => navigate('/my-appointments')} style={{ marginBottom: '20px' }}>
          &larr; Back to Appointments
        </button>

        <div className="dash-card">
          <div className="dash-card-header" style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '15px' }}>
            <h2 className="dash-card-title">Appointment Details</h2>
            <div className={`status-badge ${appointment.status.toLowerCase()}`}>
              {appointment.status}
            </div>
          </div>
          
          {error && <div className="auth-error" style={{ marginTop: '15px' }}>{error}</div>}

          <div style={{ marginTop: '20px', lineHeight: '1.6' }}>
            <p><strong>Date & Time:</strong> {new Date(appointment.appointmentDatetime).toLocaleString()}</p>
            {role === 'PATIENT' ? (
              <p><strong>Doctor:</strong> Dr. {appointment.doctorName}</p>
            ) : (
              <p><strong>Patient:</strong> {appointment.patientName}</p>
            )}
            <p><strong>Reason:</strong> {appointment.reason}</p>
            <p><strong>Notes:</strong> {appointment.notes || 'No additional notes provided.'}</p>
            <p><strong>Created On:</strong> {new Date(appointment.createdAt).toLocaleString()}</p>
          </div>

          <div style={{ marginTop: '30px', paddingTop: '20px', borderTop: '1px solid var(--border-color)' }}>
            {role === 'PATIENT' && appointment.status === 'PENDING' && (
              <div style={{ background: 'var(--surface-color)', padding: '20px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <h3>Complete Booking</h3>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: '15px' }}>
                  Please pay the consultation fee ($50.00) to confirm your appointment.
                </p>
                <button className="primaryBtn full-width" onClick={handlePayment} disabled={paying}>
                  {paying ? 'Processing...' : 'Pay with Sandbox Gateway'}
                </button>
              </div>
            )}

            {role === 'DOCTOR' && appointment.status === 'CONFIRMED' && (
              <div>
                <h3>Doctor Actions</h3>
                <button className="primaryBtn" onClick={() => handleStatusUpdate('COMPLETED')} disabled={updating}>
                  {updating ? 'Updating...' : 'Mark as Completed'}
                </button>
                <button className="secondaryBtn" onClick={() => handleStatusUpdate('CANCELLED')} disabled={updating} style={{ marginLeft: '10px' }}>
                  Cancel Appointment
                </button>
              </div>
            )}

            {/* File Upload Section */}
            <div style={{ marginTop: '30px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                <h3 style={{ margin: 0 }}>Medical Documents</h3>
                <label className="secondaryBtn small-btn" style={{ cursor: 'pointer', margin: 0 }}>
                  {uploading ? 'Uploading...' : '+ Upload Document'}
                  <input type="file" style={{ display: 'none' }} onChange={handleFileUpload} disabled={uploading} />
                </label>
              </div>

              {uploadError && <div className="auth-error" style={{ marginBottom: '15px' }}>{uploadError}</div>}

              {documents.length === 0 ? (
                <div style={{ padding: '20px', border: '2px dashed var(--border-color)', borderRadius: '8px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                  No medical documents attached yet.
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {documents.map(doc => (
                    <div key={doc.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px', backgroundColor: 'var(--surface-color)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                      <div>
                        <div style={{ fontWeight: '500', color: 'var(--text-primary)' }}>{doc.fileName}</div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                          Uploaded {new Date(doc.uploadedAt).toLocaleDateString()}
                        </div>
                      </div>
                      <a 
                        href={getDocumentDownloadUrl(doc.id)} 
                        target="_blank" 
                        rel="noreferrer"
                        className="primaryBtn small-btn" 
                        style={{ textDecoration: 'none' }}
                      >
                        Download
                      </a>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </AppLayout>
  )
}
