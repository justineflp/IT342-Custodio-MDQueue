import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getMyAppointments, getAllAppointments, getAllDoctors, approveDoctor } from '../appointment/appointmentApi'
import { apiFetch } from '../../shared/lib/api'

export default function DashboardPage() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [appointments, setAppointments] = useState([])
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  
  useEffect(() => {
    loadData()
  }, [])

  async function loadData() {
    setLoading(true)
    try {
      const profileRes = await apiFetch('/users/me')
      let currentUser = null
      if (profileRes.success) {
          currentUser = profileRes.data
          setUser(currentUser)
      }
      
      if (currentUser?.role === 'ADMIN') {
        const allAppts = await getAllAppointments()
        if (allAppts.success) setAppointments(allAppts.data || [])
        
        const allDocs = await getAllDoctors()
        if (allDocs.success) setDoctors(allDocs.data || [])
      } else {
        const apptRes = await getMyAppointments()
        if (apptRes.success) setAppointments(apptRes.data || [])
      }
    } catch (err) {
      console.error('Failed to load dashboard data', err)
    }
    setLoading(false)
  }

  async function handleDoctorApprove(id) {
    try {
      const res = await approveDoctor(id)
      if (res.success) {
        setDoctors(docs => docs.map(d => d.id === id ? {...d, isApproved: true} : d))
      }
    } catch (err) {
      console.error(err)
    }
  }

  async function handleUpdateAppointmentStatus(id, newStatus) {
    try {
      const res = await apiFetch(`/appointments/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status: newStatus })
      })
      if (res.success) {
        setAppointments(appts => appts.map(a => a.id === id ? {...a, status: newStatus} : a))
      }
    } catch (err) {
      console.error(err)
    }
  }

  const role = user?.role || 'PATIENT'

  return (
    <AppLayout>
      <div className="dash-page">
        <div className="dash-header">
          <div>
            <h1 className="dash-title">
              Welcome back, {user?.fullName || 'User'}
            </h1>
            <p className="dash-subtitle">
              {role === 'PATIENT'
                ? 'Manage your medical appointments and documents'
                : role === 'DOCTOR' 
                  ? 'View and manage your scheduled patients'
                  : 'System overview and administration'}
            </p>
          </div>
        </div>

        {loading ? (
          <div className="loading-card">Loading your dashboard…</div>
        ) : role === 'ADMIN' ? (
          <div className="admin-dashboard">
            <div className="dash-card full-width">
              <div className="dash-card-header">
                <h2 className="dash-card-title">Live System KPIs</h2>
              </div>
              <div className="clinic-grid">
                <div className="clinic-mini-card">
                    <div className="clinic-mini-name">Total Appointments</div>
                    <div className="clinic-mini-info">{appointments.length} total</div>
                </div>
                <div className="clinic-mini-card">
                    <div className="clinic-mini-name">Pending Doctors</div>
                    <div className="clinic-mini-info">{doctors.filter(d => !d.isApproved).length} pending</div>
                </div>
                <div className="clinic-mini-card">
                    <div className="clinic-mini-name">Active Doctors</div>
                    <div className="clinic-mini-info">{doctors.filter(d => d.isApproved).length} approved</div>
                </div>
              </div>
            </div>

            <div className="dash-card full-width" style={{ marginTop: '20px' }}>
              <div className="dash-card-header">
                <h2 className="dash-card-title">Pending Doctor Approvals</h2>
              </div>
              <div className="entry-list">
                {doctors.filter(d => !d.isApproved).length === 0 && <p style={{padding: '20px', color: '#6b7280'}}>No pending approvals.</p>}
                {doctors.filter(d => !d.isApproved).map(doc => (
                  <div key={doc.id} className="entry-row">
                    <div className="entry-info">
                      <div className="entry-clinic">Dr. {doc.fullName}</div>
                      <div className="entry-queue">{doc.email}</div>
                    </div>
                    <div className="entry-right" style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                      <button 
                        className="primaryBtn small-btn" 
                        onClick={() => handleDoctorApprove(doc.id)}
                        style={{ padding: '6px 16px', background: '#3b82f6', fontSize: '0.85rem' }}
                      >Approve</button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        ) : (
          <div className="dash-grid">
            {/* Appointments Card */}
            <div className="dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="20" height="20"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                  {role === 'PATIENT' ? 'Upcoming Appointments' : 'Your Schedule'}
                </h2>
                <span className="badge">{appointments.length}</span>
              </div>
              
              {appointments.length === 0 ? (
                <div className="empty-state">
                  <p>{role === 'PATIENT' ? 'You have no upcoming appointments.' : 'No appointments scheduled.'}</p>
                  {role === 'PATIENT' && (
                    <Link to="/book-appointment" className="primaryBtn small-btn">Book Now</Link>
                  )}
                </div>
              ) : (
                <div className="entry-list">
                  {appointments.slice(0, 5).map(appt => (
                    <div key={appt.id} className="entry-row" onClick={() => navigate(`/appointments/${appt.id}`)} style={{ cursor: 'pointer' }}>
                      <div className="entry-info">
                        <div className="entry-clinic">{role === 'PATIENT' ? `Dr. ${appt.doctorName}` : `Patient: ${appt.patientName}`}</div>
                        <div className="entry-queue">{new Date(appt.appointmentDatetime).toLocaleString()}</div>
                      </div>
                      <div className="entry-right" style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                        <div className={`status-badge ${appt.status.toLowerCase()}`}>
                          {appt.status}
                        </div>
                        {role === 'DOCTOR' && appt.status === 'PENDING' && (
                          <div style={{ display: 'flex', gap: '6px' }}>
                            <button 
                              className="primaryBtn small-btn" 
                              onClick={(e) => { e.stopPropagation(); handleUpdateAppointmentStatus(appt.id, 'CONFIRMED'); }}
                              style={{ padding: '6px 12px', fontSize: '0.8rem', background: '#10b981' }}
                            >Confirm</button>
                            <button 
                              className="secondaryBtn small-btn" 
                              onClick={(e) => { e.stopPropagation(); handleUpdateAppointmentStatus(appt.id, 'CANCELLED'); }}
                              style={{ padding: '6px 12px', fontSize: '0.8rem', background: '#ef4444', color: 'white', border: 'none' }}
                            >Cancel</button>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                  {appointments.length > 5 && (
                    <Link to="/my-appointments" style={{ display: 'block', textAlign: 'center', marginTop: '10px', fontSize: '0.9rem' }}>
                      View all {appointments.length} appointments
                    </Link>
                  )}
                </div>
              )}
            </div>

            {/* Quick Actions */}
            <div className="dash-card">
              <div className="dash-card-header">
                <h2 className="dash-card-title">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="20" height="20"><path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/></svg>
                  Quick Actions
                </h2>
              </div>
              <div className="quick-actions">
                {role === 'PATIENT' && (
                    <Link to="/book-appointment" className="action-card">
                      <div className="action-icon blue">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                      </div>
                      <span>Book Appointment</span>
                    </Link>
                )}
                <Link to="/my-appointments" className="action-card">
                  <div className="action-icon green">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                  </div>
                  <span>All Appointments</span>
                </Link>
                <Link to="/profile" className="action-card">
                  <div className="action-icon purple">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="24" height="24"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  </div>
                  <span>My Profile</span>
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  )
}
