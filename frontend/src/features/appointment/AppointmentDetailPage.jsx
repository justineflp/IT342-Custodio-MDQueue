import { useState, useEffect } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { 
  getAppointmentDetails, 
  updateAppointmentStatus, 
  uploadDocument, 
  getDocuments, 
  getDocumentDownloadUrl,
  getPaymentDetails
} from './appointmentApi'
import { apiFetch } from '../../shared/lib/api'

export default function AppointmentDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  
  const [appointment, setAppointment] = useState(null)
  const [payment, setPayment] = useState(null)
  const [documents, setDocuments] = useState([])
  
  const [loading, setLoading] = useState(true)
  const [role, setRole] = useState('PATIENT')
  const [updating, setUpdating] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')
  const [amountDueInput, setAmountDueInput] = useState('')

  const handleDoctorConfirm = async () => {
    if (!amountDueInput || isNaN(amountDueInput) || parseFloat(amountDueInput) <= 0) {
      alert('Please enter a valid billing amount.')
      return
    }
    setUpdating(true)
    setError('')
    setSuccessMsg('')
    try {
      const res = await apiFetch(`/appointments/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({
          status: 'CONFIRMED',
          amountDue: parseFloat(amountDueInput).toFixed(2)
        })
      })
      if (res.success) {
        setAppointment(res.data)
        setSuccessMsg('Consultation confirmed and amount due set successfully!')
        await loadDetails()
      } else {
        setError(res.message || 'Failed to confirm appointment')
      }
    } catch (err) {
      setError('An error occurred during confirmation.')
    }
    setUpdating(false)
  }
  
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState('')

  async function loadDetails() {
    setLoading(true)
    const userRes = await apiFetch('/users/me')
    if (userRes.success) setRole(userRes.data.role)

    const apptRes = await getAppointmentDetails(id)
    if (apptRes.success) {
      const apptData = apptRes.data
      setAppointment(apptData)
      
      // Fetch documents
      const docRes = await getDocuments(apptData.id)
      if (docRes.success) {
        setDocuments(docRes.data || [])
      }

      // Fetch payment details
      const paymentRes = await getPaymentDetails(apptData.id)
      if (paymentRes.success) {
        setPayment(paymentRes.data || null)
      }
    } else {
      setError('Appointment not found or you are not authorized to view it.')
    }
    setLoading(false)
  }

  useEffect(() => {
    loadDetails()
    
    // Check if redirected from payment page with success flag
    if (location.state?.paymentSuccess) {
      setSuccessMsg('💳 Payment processed successfully! Your appointment has been confirmed.')
      // clear location state
      navigate(location.pathname, { replace: true, state: {} })
    }
  }, [id])

  const handleStatusUpdate = async (status) => {
    if (status === 'CANCELLED' && !window.confirm('Are you sure you want to cancel this appointment?')) return
    setUpdating(true)
    setError('')
    setSuccessMsg('')
    try {
      const res = await updateAppointmentStatus(id, status)
      if (res.success) {
        setAppointment(res.data)
        if (status === 'CANCELLED') {
          setSuccessMsg('Appointment has been successfully cancelled.')
        } else {
          setSuccessMsg(`Appointment updated to ${status}.`)
        }
        // reload details to update payment/other entities
        await loadDetails()
      } else {
        setError(res.message || 'Failed to update status')
      }
    } catch (err) {
      setError('An error occurred during status modification.')
    }
    setUpdating(false)
  }

  const handleFileUpload = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    
    setUploading(true)
    setUploadError('')
    setSuccessMsg('')
    try {
      const res = await uploadDocument(id, file)
      if (res.success) {
        setDocuments([res.data, ...documents])
        setSuccessMsg('Document attached successfully!')
      } else {
        setUploadError(res.message || 'Failed to upload document')
      }
    } catch (err) {
      setUploadError('An error occurred while uploading the file.')
    }
    setUploading(false)
    e.target.value = null // reset input
  }

  if (loading) return <AppLayout><div className="loading-card">Loading consultation workspace...</div></AppLayout>
  if (!appointment) return <AppLayout><div className="dash-page"><div className="auth-error">{error || 'Appointment not found.'}</div></div></AppLayout>

  const apptDate = new Date(appointment.appointmentDatetime)
  const isUpcoming = apptDate > new Date()
  const isPending = appointment.status === 'PENDING'
  const isConfirmed = appointment.status === 'CONFIRMED'
  const canCancel = (isPending || isConfirmed) && isUpcoming && role !== 'ADMIN'

  return (
    <AppLayout>
      <div className="dash-page" style={{ maxWidth: '900px', margin: '0 auto' }}>
        <button 
          className="secondaryBtn small-btn" 
          onClick={() => navigate('/my-appointments')} 
          style={{ marginBottom: '20px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
        >
          &larr; Back to My Appointments
        </button>

        {successMsg && (
          <div className="alert alert-success" style={{ marginBottom: '20px', borderRadius: '12px', padding: '12px 16px' }}>
            <span>{successMsg}</span>
            <button className="alert-close" onClick={() => setSuccessMsg('')}>&times;</button>
          </div>
        )}

        {error && (
          <div className="auth-error" style={{ marginBottom: '20px', borderRadius: '12px', padding: '12px 16px' }}>
            ⚠️ {error}
          </div>
        )}

        <div className="dash-card" style={{ padding: '24px', border: '1px solid var(--border)', boxShadow: 'var(--shadow)' }}>
          {/* Header Section */}
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center', 
            borderBottom: '1px solid var(--border)', 
            paddingBottom: '20px',
            marginBottom: '24px',
            flexWrap: 'wrap',
            gap: '12px'
          }}>
            <div>
              <span style={{ fontSize: '0.8rem', color: 'var(--muted)', fontWeight: '700', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Consultation Details</span>
              <h2 style={{ margin: '4px 0 0 0', fontSize: '1.6rem', fontWeight: '800' }}>Appointment Details</h2>
            </div>
            <span className={`status-badge large ${appointment.status.toLowerCase()}`}>
              {appointment.status}
            </span>
          </div>

          {/* Grid Information Layout */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginBottom: '32px' }}>
            
            {/* COLUMN 1: Appointment Info */}
            <div style={{
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: '10px',
              padding: '20px'
            }}>
              <h3 style={{ margin: '0 0 16px 0', fontSize: '1rem', fontWeight: '700', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px', color: 'var(--text)' }}>
                🏥 Consultation Info
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', fontSize: '0.92rem' }}>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Doctor Name:</span>
                  <span style={{ fontWeight: '700', color: 'var(--text)' }}>Dr. {appointment.doctorName}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Medical Specialty:</span>
                  <span style={{ fontWeight: '600', color: 'var(--blue)' }}>{appointment.doctorSpecialty}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Date & Time:</span>
                  <span style={{ fontWeight: '600', color: 'var(--text)' }}>
                    {apptDate.toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })} at {apptDate.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Reason for Visit:</span>
                  <span style={{ color: 'var(--text)' }}>{appointment.reason}</span>
                </div>
                {appointment.notes && (
                  <div>
                    <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Clinical Notes:</span>
                    <span style={{ color: 'var(--text)', fontStyle: 'italic' }}>{appointment.notes}</span>
                  </div>
                )}
              </div>
            </div>

            {/* COLUMN 2: Patient Info */}
            <div style={{
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              borderRadius: '10px',
              padding: '20px'
            }}>
              <h3 style={{ margin: '0 0 16px 0', fontSize: '1rem', fontWeight: '700', borderBottom: '1px solid #e2e8f0', paddingBottom: '8px', color: 'var(--text)' }}>
                👤 Patient Information <span style={{ fontSize: '0.75rem', fontWeight: '500', color: 'var(--muted)', marginLeft: '4px' }}>(Read-Only)</span>
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', fontSize: '0.92rem' }}>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Full Name:</span>
                  <span style={{ fontWeight: '700', color: 'var(--text)' }}>{appointment.patientName}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Email Address:</span>
                  <span style={{ fontWeight: '500', color: 'var(--text)' }}>{appointment.patientEmail || 'Not Available'}</span>
                </div>
                <div>
                  <span style={{ color: 'var(--muted)', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase' }}>Phone Number:</span>
                  <span style={{ fontWeight: '500', color: 'var(--text)' }}>{appointment.patientPhone || 'Not Available'}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Section: Uploaded Documents */}
          <div style={{ borderTop: '1px solid var(--border)', paddingTop: '24px', marginBottom: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <h3 style={{ margin: 0, fontSize: '1.1rem', fontWeight: '700' }}>📂 Uploaded Medical Documents</h3>
              <label 
                className="primaryBtn small-btn" 
                style={{ 
                  cursor: 'pointer', 
                  margin: 0, 
                  fontSize: '0.8rem', 
                  height: '34px',
                  display: 'inline-flex', 
                  alignItems: 'center', 
                  gap: '6px' 
                }}
              >
                {uploading ? 'Attaching file...' : '➕ Upload Document'}
                <input type="file" style={{ display: 'none' }} onChange={handleFileUpload} disabled={uploading} />
              </label>
            </div>

            {uploadError && (
              <div className="auth-error" style={{ marginBottom: '15px', borderRadius: '8px', padding: '10px 14px' }}>
                ⚠️ {uploadError}
              </div>
            )}

            {documents.length === 0 ? (
              <div style={{ 
                padding: '30px', 
                border: '2px dashed var(--border)', 
                borderRadius: '10px', 
                textAlign: 'center', 
                color: 'var(--muted)',
                fontSize: '0.9rem'
              }}>
                No medical documents attached to this consultation record yet.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {documents.map(doc => {
                  const uploadDate = new Date(doc.uploadedAt).toLocaleDateString(undefined, {
                    month: 'short',
                    day: 'numeric',
                    year: 'numeric'
                  })
                  return (
                    <div 
                      key={doc.id} 
                      style={{ 
                        display: 'flex', 
                        justifyContent: 'space-between', 
                        alignItems: 'center', 
                        padding: '12px 18px', 
                        backgroundColor: '#ffffff', 
                        borderRadius: '10px', 
                        border: '1px solid var(--border)',
                        boxShadow: '0 1px 2px rgba(0,0,0,0.02)'
                      }}
                    >
                      <div>
                        <div style={{ fontWeight: '600', color: 'var(--text)', fontSize: '0.9rem' }}>{doc.fileName}</div>
                        <div style={{ fontSize: '0.78rem', color: 'var(--muted)', marginTop: '4px' }}>
                          Type: <span style={{ fontWeight: '500', color: 'var(--text)' }}>{doc.fileType || 'Unknown'}</span> | Uploaded: {uploadDate}
                        </div>
                      </div>
                      <a 
                        href={getDocumentDownloadUrl(doc.id)} 
                        target="_blank" 
                        rel="noreferrer"
                        className="outlineBtn small" 
                        style={{ textDecoration: 'none', height: '32px', padding: '0 12px', fontSize: '0.8rem' }}
                      >
                        Download
                      </a>
                    </div>
                  )
                })}
              </div>
            )}
          </div>

          {/* Section: Payment Info */}
          <div style={{ 
            borderTop: '1px solid var(--border)', 
            paddingTop: '24px', 
            marginBottom: '16px',
            backgroundColor: payment ? (payment.status === 'SUCCESS' ? '#f0fdf4' : '#eff6ff') : '#fffbeb',
            border: payment ? (payment.status === 'SUCCESS' ? '1px solid #bbf7d0' : '1px solid #bfdbfe') : '1px solid #fef3c7',
            borderRadius: '10px',
            padding: '20px'
          }}>
            <h3 style={{ margin: '0 0 14px 0', fontSize: '1.1rem', fontWeight: '700', color: payment ? (payment.status === 'SUCCESS' ? '#166534' : '#1e40af') : '#92400e' }}>
              💳 Payment Information
            </h3>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', fontSize: '0.92rem' }}>
              <div>
                <span style={{ color: payment ? (payment.status === 'SUCCESS' ? '#14532d' : '#1e3a8a') : '#78350f', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase', opacity: '0.8' }}>Status:</span>
                <span style={{ fontWeight: '700', textTransform: 'uppercase' }}>
                  {payment ? (
                    payment.status === 'SUCCESS' ? (
                      <span style={{ color: 'var(--green)' }}>Paid ({payment.provider})</span>
                    ) : (
                      <span style={{ color: '#d97706' }}>Pay in Person (Pending)</span>
                    )
                  ) : (
                    <span style={{ color: 'var(--danger)' }}>Not Paid</span>
                  )}
                </span>
              </div>
              
              <div>
                <span style={{ color: payment ? (payment.status === 'SUCCESS' ? '#14532d' : '#1e3a8a') : '#78350f', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase', opacity: '0.8' }}>Consultation Fee:</span>
                <span style={{ fontWeight: '700', color: 'var(--text)' }}>
                  {appointment.amountDue != null ? `₱${parseFloat(appointment.amountDue).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : 'Not set yet (Pending)'}
                </span>
              </div>

              {payment && (
                <>
                  <div>
                    <span style={{ color: payment.status === 'SUCCESS' ? '#14532d' : '#1e3a8a', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase', opacity: '0.8' }}>Payment Date:</span>
                    <span style={{ fontWeight: '600', color: 'var(--text)' }}>
                      {payment.paidAt ? (
                        `${new Date(payment.paidAt).toLocaleDateString()} at ${new Date(payment.paidAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
                      ) : (
                        'To be paid at Clinic'
                      )}
                    </span>
                  </div>
                  <div>
                    <span style={{ color: payment.status === 'SUCCESS' ? '#14532d' : '#1e3a8a', display: 'block', fontSize: '0.8rem', fontWeight: '600', textTransform: 'uppercase', opacity: '0.8' }}>Gateway Reference:</span>
                    <span style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'var(--muted)' }}>
                      {payment.providerPaymentId ? payment.providerPaymentId.substring(0, 18) : 'Pending_Clinic_Ref'}...
                    </span>
                  </div>
                </>
              )}
            </div>
          </div>

          {/* Bottom Control Buttons Panels */}
          <div style={{ 
            borderTop: '1px solid var(--border)', 
            paddingTop: '24px', 
            marginTop: '24px', 
            display: 'flex', 
            flexDirection: 'column',
            gap: '20px'
          }}>
            {role === 'DOCTOR' && (appointment.status === 'PENDING' || (appointment.status === 'CONFIRMED' && !payment)) && (
              <div style={{ background: '#f8fafc', padding: '20px', borderRadius: '10px', border: '1px solid #e2e8f0', width: '100%' }}>
                <h3 style={{ margin: '0 0 10px 0', fontSize: '1.05rem', fontWeight: '700', color: 'var(--text)' }}>
                  🏥 {appointment.status === 'PENDING' ? 'Review & Confirm Booking' : 'Update Consultation Fee'}
                </h3>
                <p style={{ fontSize: '0.88rem', color: 'var(--muted)', margin: '0 0 16px 0' }}>
                  Please specify the custom consultation fee/check-up amount due for this appointment.
                </p>
                <div style={{ display: 'flex', gap: '14px', alignItems: 'center', flexWrap: 'wrap' }}>
                  <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                    <label htmlFor="amountDueInput" style={{ fontWeight: '700', fontSize: '0.85rem', color: 'var(--text)' }}>Amount Due (PHP):</label>
                    <input 
                      type="number" 
                      id="amountDueInput"
                      placeholder="e.g. 1500.00" 
                      value={amountDueInput}
                      onChange={(e) => setAmountDueInput(e.target.value)}
                      style={{
                        height: '38px',
                        width: '140px',
                        borderRadius: '8px',
                        border: '1px solid var(--border)',
                        padding: '0 10px',
                        fontSize: '0.9rem',
                        color: 'var(--text)',
                        outline: 'none'
                      }}
                    />
                  </div>
                  <button 
                    className="primaryBtn small-btn" 
                    onClick={handleDoctorConfirm} 
                    disabled={updating}
                    style={{ backgroundColor: 'var(--blue)', border: 'none' }}
                  >
                    {updating ? 'Processing...' : (appointment.status === 'PENDING' ? '✔️ Confirm & Set Bill' : '✔️ Update Fee')}
                  </button>
                  {appointment.status === 'PENDING' && (
                    <button 
                      className="outlineBtn small" 
                      onClick={() => handleStatusUpdate('CANCELLED')} 
                      disabled={updating}
                      style={{ color: 'var(--danger)', borderColor: 'var(--danger-border)' }}
                    >
                      Cancel Booking
                    </button>
                  )}
                </div>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px', width: '100%' }}>
              <div>
                {role === 'DOCTOR' && isConfirmed && (
                  <button 
                    className="primaryBtn" 
                    onClick={() => handleStatusUpdate('COMPLETED')} 
                    disabled={updating}
                    style={{ height: '40px', padding: '0 20px', fontSize: '0.88rem' }}
                  >
                    {updating ? 'Processing...' : 'Mark as Completed'}
                  </button>
                )}
              </div>

            <div style={{ display: 'inline-flex', gap: '12px' }}>
              {/* Pay Now Button */}
              {role === 'PATIENT' && isConfirmed && !payment && (
                <button 
                  className="primaryBtn" 
                  onClick={() => navigate(`/appointments/${id}/payment`)}
                  style={{ 
                    height: '40px', 
                    padding: '0 24px', 
                    fontSize: '0.88rem', 
                    backgroundColor: 'var(--green)',
                    boxShadow: '0 4px 10px rgba(22,163,74,0.25)' 
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#15803d'}
                  onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'var(--green)'}
                >
                  💳 Pay Now (₱{appointment.amountDue != null ? parseFloat(appointment.amountDue).toLocaleString('en-US', { minimumFractionDigits: 2 }) : '0.00'})
                </button>
              )}

              {/* Cancel Appointment Button */}
              {canCancel && (
                <button 
                  className="outlineBtn" 
                  onClick={() => handleStatusUpdate('CANCELLED')} 
                  disabled={updating}
                  style={{ 
                    height: '40px', 
                    padding: '0 20px', 
                    fontSize: '0.88rem',
                    color: 'var(--danger)',
                    borderColor: 'var(--danger-border)'
                  }}
                  onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = 'var(--danger-bg)'; }}
                  onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
                >
                  {updating ? 'Cancelling...' : 'Cancel Appointment'}
                </button>
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  </AppLayout>
  )
}
