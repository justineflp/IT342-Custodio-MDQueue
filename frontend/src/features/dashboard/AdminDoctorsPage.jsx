import { useState, useEffect } from 'react'
import AppLayout from '../../shared/components/AppLayout'
import { getAllDoctors } from '../appointment/appointmentApi'

export default function AdminDoctorsPage() {
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function load() {
      setLoading(true)
      try {
        const res = await getAllDoctors()
        if (res.success && res.data) {
          // Only show approved doctors
          setDoctors(res.data.filter(d => d.isApproved))
        }
      } catch (err) {
        console.error('Failed to load doctors', err)
      }
      setLoading(false)
    }
    load()
  }, [])

  return (
    <AppLayout>
      <div className="dash-page">
        <div className="dash-header">
          <div>
            <h1 className="dash-title">Approved Doctors</h1>
            <p className="dash-subtitle">A list of all officially approved doctors in the system.</p>
          </div>
        </div>

        {loading ? (
          <div className="loading-card">Loading doctors...</div>
        ) : (
          <div className="dash-card full-width">
            <div className="dash-card-header">
              <h2 className="dash-card-title">Doctor Roster</h2>
            </div>
            <div className="entry-list">
              {doctors.length === 0 && <p style={{padding: '20px', color: '#6b7280'}}>No approved doctors found.</p>}
              {doctors.map(doc => (
                <div key={doc.id} className="entry-row">
                  <div className="entry-info">
                    <div className="entry-clinic">Dr. {doc.fullName}</div>
                    <div className="entry-queue">{doc.email}</div>
                  </div>
                  <div className="entry-right">
                    <div className="status-badge completed">APPROVED</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  )
}
