import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getMyAppointments, getAllAppointments } from './appointmentApi'
import { apiFetch } from '../../shared/lib/api'

export default function AppointmentsPage() {
  const navigate = useNavigate()
  const [appointments, setAppointments] = useState([])
  const [loading, setLoading] = useState(true)
  const [role, setRole] = useState('PATIENT')

  useEffect(() => {
    async function load() {
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
        let list = apptRes.data || []
        if (currentRole === 'ADMIN') {
          list = list.filter(a => a.status === 'COMPLETED')
        }
        setAppointments(list)
      }
      setLoading(false)
    }
    load()
  }, [])

  return (
    <AppLayout>
      <div className="dash-page">
        <div className="dash-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 className="dash-title">{role === 'PATIENT' ? 'My Appointments' : 'Scheduled Appointments'}</h1>
            <p className="dash-subtitle">View and manage all your appointment records.</p>
          </div>
          {role === 'PATIENT' && (
              <Link to="/book-appointment" className="primaryBtn small-btn">Book New</Link>
          )}
        </div>

        {loading ? (
          <div className="loading-card">Loading appointments…</div>
        ) : appointments.length === 0 ? (
          <div className="empty-state">
            <p>No appointments found.</p>
          </div>
        ) : (
          <div className="clinic-grid" style={{ gridTemplateColumns: '1fr' }}>
            {appointments.map(appt => (
              <div key={appt.id} className="dash-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer' }} onClick={() => navigate(`/appointments/${appt.id}`)}>
                <div className="entry-info">
                  <h3 style={{ margin: 0, fontSize: '1.2rem' }}>{new Date(appt.appointmentDatetime).toLocaleString()}</h3>
                  <p style={{ margin: '5px 0', color: 'var(--text-secondary)' }}>
                    {role === 'PATIENT' ? `Doctor: ${appt.doctorName}` : `Patient: ${appt.patientName}`}
                  </p>
                  <p style={{ margin: 0, fontSize: '0.9rem' }}>Reason: {appt.reason}</p>
                </div>
                <div className="entry-right" style={{ textAlign: 'right' }}>
                  <div className={`status-badge ${appt.status.toLowerCase()}`} style={{ marginBottom: '10px' }}>
                    {appt.status}
                  </div>
                  <button className="primaryBtn small-btn" onClick={(e) => { e.stopPropagation(); navigate(`/appointments/${appt.id}`) }}>View Details</button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  )
}
