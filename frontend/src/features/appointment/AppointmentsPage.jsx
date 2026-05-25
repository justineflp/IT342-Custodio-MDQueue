import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getMyAppointments, getAllAppointments, updateAppointmentStatus } from './appointmentApi'
import { apiFetch } from '../../shared/lib/api'

export default function AppointmentsPage() {
  const navigate = useNavigate()
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  
  // Advanced filters
  const [statusFilter, setStatusFilter] = useState('ALL') // ALL, UPCOMING, PAST, CANCELLED
  const [startDate, setStartDate] = useState('')
  const [endDate, setEndDate] = useState('')
  
  const [role, setRole] = useState('PATIENT')
  const [updatingId, setUpdatingId] = useState(null)
  const [actionError, setActionError] = useState('')

  async function loadData() {
    setLoading(true)
    const userRes = await apiFetch('/users/me')
    let currentRole = 'PATIENT'
    if (userRes.success) {
      currentRole = userRes.data.role
      setRole(currentRole)
    }

    let apptRes
    if (currentRole === 'ADMIN') {
      apptRes = await getAllAppointments()
    } else {
      apptRes = await getMyAppointments()
    }

    if (apptRes.success) {
      setAppointments(apptRes.data || [])
    }
    setLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleCancelAppointment = async (apptId) => {
    if (!window.confirm('Are you sure you want to cancel this appointment?')) return
    setUpdatingId(apptId)
    setActionError('')
    try {
      const res = await updateAppointmentStatus(apptId, 'CANCELLED')
      if (res.success) {
        // Reload list
        await loadData()
      } else {
        setActionError(res.message || 'Failed to cancel appointment')
      }
    } catch (err) {
      setActionError('An error occurred while cancelling the appointment.')
    }
    setUpdatingId(null)
  }

  // Filter Logic
  const filteredAppointments = appointments.filter(appt => {
    const apptDate = new Date(appt.appointmentDatetime)
    const now = new Date()

    // 1. Status Filter
    if (statusFilter === 'UPCOMING') {
      // status is PENDING/CONFIRMED and datetime is in the future
      if (appt.status === 'CANCELLED' || appt.status === 'COMPLETED') return false;
      if (apptDate <= now) return false;
    } else if (statusFilter === 'PAST') {
      // completed, or pending/confirmed in the past
      if (appt.status === 'CANCELLED') return false;
      if (appt.status !== 'COMPLETED' && apptDate > now) return false;
    } else if (statusFilter === 'CANCELLED') {
      if (appt.status !== 'CANCELLED') return false;
    }

    // 2. Date Range Filter
    if (startDate) {
      const start = new Date(startDate)
      start.setHours(0,0,0,0)
      if (apptDate < start) return false;
    }
    if (endDate) {
      const end = new Date(endDate)
      end.setHours(23,59,59,999)
      if (apptDate > end) return false;
    }

    return true
  })

  // Count helper for badge indicators
  const getCount = (filter) => {
    return appointments.filter(appt => {
      const apptDate = new Date(appt.appointmentDatetime)
      const now = new Date()
      if (filter === 'ALL') return true;
      if (filter === 'UPCOMING') {
        return (appt.status !== 'CANCELLED' && appt.status !== 'COMPLETED' && apptDate > now);
      }
      if (filter === 'PAST') {
        return (appt.status === 'COMPLETED' || (appt.status !== 'CANCELLED' && apptDate <= now));
      }
      if (filter === 'CANCELLED') {
        return appt.status === 'CANCELLED';
      }
      return false;
    }).length;
  }

  return (
    <AppLayout>
      <div className="dash-page">
        <div className="dash-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <div>
            <h1 className="dash-title">
              {role === 'PATIENT' ? 'My Appointments' : role === 'DOCTOR' ? 'Scheduled Appointments' : 'Completed System Records'}
            </h1>
            <p className="dash-subtitle">View and manage consultation logs.</p>
          </div>
          {role === 'PATIENT' && (
            <Link to="/book-appointment" className="primaryBtn small-btn" style={{ textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              ➕ Book New Appointment
            </Link>
          )}
        </div>

        {actionError && (
          <div className="auth-error" style={{ marginBottom: '20px', padding: '12px 16px', borderRadius: '8px' }}>
            ⚠️ {actionError}
          </div>
        )}

        {/* Filter and Query Section */}
        <div style={{
          backgroundColor: 'var(--card)',
          borderRadius: 'var(--radius)',
          border: '1px solid var(--border)',
          padding: '20px',
          boxShadow: 'var(--shadow)',
          marginBottom: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '16px'
        }}>
          {/* Status Tabs */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginRight: '8px' }}>Filter Status:</span>
            {[
              { value: 'ALL', label: 'All Records' },
              { value: 'UPCOMING', label: 'Upcoming' },
              { value: 'PAST', label: 'Past / Completed' },
              { value: 'CANCELLED', label: 'Cancelled' }
            ].map(tab => {
              const isActive = statusFilter === tab.value;
              return (
                <button
                  key={tab.value}
                  onClick={() => setStatusFilter(tab.value)}
                  style={{
                    padding: '8px 16px',
                    borderRadius: '20px',
                    border: isActive ? '1px solid var(--blue)' : '1px solid var(--border)',
                    background: isActive ? 'var(--blue-light)' : '#ffffff',
                    color: isActive ? 'var(--blue)' : 'var(--muted)',
                    fontSize: '0.85rem',
                    fontWeight: '600',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                    outline: 'none',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px'
                  }}
                >
                  {tab.label}
                  <span style={{
                    fontSize: '0.75rem',
                    background: isActive ? 'rgba(37, 99, 235, 0.15)' : 'rgba(0, 0, 0, 0.05)',
                    color: isActive ? 'var(--blue)' : 'var(--muted)',
                    padding: '1px 6px',
                    borderRadius: '10px',
                    fontWeight: '700'
                  }}>
                    {getCount(tab.value)}
                  </span>
                </button>
              );
            })}
          </div>

          {/* Date Picker Controls */}
          <div style={{ display: 'flex', gap: '16px', flexWrap: 'wrap', alignItems: 'center', borderTop: '1px solid #f1f5f9', paddingTop: '16px' }}>
            <span style={{ fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em', marginRight: '8px' }}>Date Range:</span>
            
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
              <label htmlFor="startDate" style={{ fontSize: '0.85rem', color: 'var(--muted)', fontWeight: '500' }}>From:</label>
              <input 
                type="date" 
                id="startDate"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                style={{
                  height: '36px',
                  borderRadius: '8px',
                  border: '1px solid var(--border)',
                  padding: '0 8px',
                  fontSize: '0.85rem',
                  outline: 'none',
                  color: 'var(--text)'
                }}
              />
            </div>

            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
              <label htmlFor="endDate" style={{ fontSize: '0.85rem', color: 'var(--muted)', fontWeight: '500' }}>To:</label>
              <input 
                type="date" 
                id="endDate"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                style={{
                  height: '36px',
                  borderRadius: '8px',
                  border: '1px solid var(--border)',
                  padding: '0 8px',
                  fontSize: '0.85rem',
                  outline: 'none',
                  color: 'var(--text)'
                }}
              />
            </div>

            {(startDate || endDate) && (
              <button 
                onClick={() => { setStartDate(''); setEndDate(''); }}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--danger)',
                  fontSize: '0.85rem',
                  fontWeight: '600',
                  cursor: 'pointer',
                  padding: '0',
                  marginLeft: '8px'
                }}
              >
                Clear Range
              </button>
            )}
          </div>
        </div>

        {/* Tabular List Section */}
        {loading ? (
          <div className="loading-card">Synchronizing appointments log...</div>
        ) : filteredAppointments.length === 0 ? (
          <div className="empty-state-card">
            <p style={{ margin: '0' }}>No consultations found matching your filters.</p>
          </div>
        ) : (
          <div className="dash-card" style={{ padding: '0', overflowX: 'auto', border: '1px solid var(--border)', boxShadow: 'var(--shadow)' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', minWidth: '700px' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid var(--border)', backgroundColor: '#f8fafc' }}>
                  <th style={{ padding: '16px 20px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Date</th>
                  <th style={{ padding: '16px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Time</th>
                  {role !== 'DOCTOR' && (
                    <th style={{ padding: '16px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Doctor</th>
                  )}
                  {role !== 'PATIENT' && (
                    <th style={{ padding: '16px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Patient</th>
                  )}
                  <th style={{ padding: '16px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Status</th>
                  <th style={{ padding: '16px 20px', fontSize: '0.8rem', fontWeight: '700', color: 'var(--muted)', textTransform: 'uppercase', letterSpacing: '0.05em', textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filteredAppointments.map(appt => {
                  const apptDate = new Date(appt.appointmentDatetime)
                  const dateStr = apptDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })
                  const timeStr = apptDate.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })
                  
                  const isUpcoming = apptDate > new Date()
                  const isPending = appt.status === 'PENDING'
                  const isConfirmed = appt.status === 'CONFIRMED'
                  const canCancel = (isPending || isConfirmed) && isUpcoming && role !== 'ADMIN'

                  return (
                    <tr 
                      key={appt.id} 
                      style={{ 
                        borderBottom: '1px solid #f1f5f9', 
                        transition: 'all 0.15s ease',
                      }}
                      onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#fafbfc'}
                      onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                    >
                      <td style={{ padding: '16px 20px', fontSize: '0.9rem', fontWeight: '600', color: 'var(--text)' }}>{dateStr}</td>
                      <td style={{ padding: '16px', fontSize: '0.9rem', color: 'var(--muted)' }}>{timeStr}</td>
                      {role !== 'DOCTOR' && (
                        <td style={{ padding: '16px', fontSize: '0.9rem', fontWeight: '500', color: 'var(--text)' }}>Dr. {appt.doctorName}</td>
                      )}
                      {role !== 'PATIENT' && (
                        <td style={{ padding: '16px', fontSize: '0.9rem', fontWeight: '500', color: 'var(--text)' }}>{appt.patientName}</td>
                      )}
                      <td style={{ padding: '16px' }}>
                        <span className={`status-badge ${appt.status.toLowerCase()}`}>
                          {appt.status}
                        </span>
                      </td>
                      <td style={{ padding: '12px 20px', textAlign: 'right', whiteSpace: 'nowrap' }}>
                        <div style={{ display: 'inline-flex', gap: '8px', justifyContent: 'flex-end', width: '100%' }}>
                          {/* View Button */}
                          <button
                            className="outlineBtn small"
                            onClick={() => navigate(`/appointments/${appt.id}`)}
                            style={{ padding: '0 12px', height: '32px', fontSize: '0.8rem' }}
                          >
                            View
                          </button>

                          {/* Pay Button */}
                          {role === 'PATIENT' && isPending && (
                            <button
                              className="primaryBtn small-btn"
                              onClick={() => navigate(`/appointments/${appt.id}/payment`)}
                              style={{ 
                                padding: '0 12px', 
                                height: '32px', 
                                fontSize: '0.8rem', 
                                backgroundColor: 'var(--green)',
                                border: 'none'
                              }}
                              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#15803d'}
                              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'var(--green)'}
                            >
                              Pay Now
                            </button>
                          )}

                          {/* Cancel Button */}
                          {canCancel && (
                            <button
                              className="outlineBtn small"
                              onClick={() => handleCancelAppointment(appt.id)}
                              disabled={updatingId === appt.id}
                              style={{ 
                                padding: '0 12px', 
                                height: '32px', 
                                fontSize: '0.8rem',
                                color: 'var(--danger)',
                                borderColor: 'var(--danger-border)'
                              }}
                              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = 'var(--danger-bg)'; }}
                              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; }}
                            >
                              {updatingId === appt.id ? '...' : 'Cancel'}
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AppLayout>
  )
}
