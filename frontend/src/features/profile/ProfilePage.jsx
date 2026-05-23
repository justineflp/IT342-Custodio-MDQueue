import { useEffect, useState } from 'react'
import AppLayout from '../../shared/components/AppLayout'
import { getUserProfile } from '../queue/queueApi'
import { getMyEntries } from '../queue/queueApi'
import { updateSpecialty } from '../appointment/appointmentApi'

export default function ProfilePage() {
  const [user, setUser] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [specialty, setSpecialty] = useState('')
  const [saving, setSaving] = useState(false)
  const [successMsg, setSuccessMsg] = useState('')

  const specialties = [
    "General Practice",
    "Cardiology",
    "Dermatology",
    "Pediatrics",
    "Neurology",
    "Psychiatry",
    "Orthopedics",
    "Oncology",
    "Gynecology",
    "Ophthalmology"
  ]


  useEffect(() => {
    async function load() {
      setLoading(true)
      const [profileRes, historyRes] = await Promise.all([
        getUserProfile(),
        getMyEntries()
      ])
      if (profileRes.success) {
        setUser(profileRes.data)
        if (profileRes.data.specialty) {
          setSpecialty(profileRes.data.specialty)
        }
      }
      if (historyRes.success) setHistory(historyRes.data || [])
      setLoading(false)
    }
    load()
  }, [])

  const handleUpdateSpecialty = async () => {
    setSaving(true)
    setSuccessMsg('')
    try {
      const res = await updateSpecialty(specialty)
      if (res.success) {
        setSuccessMsg('Specialty updated successfully!')
        setUser({ ...user, specialty })
        setTimeout(() => setSuccessMsg(''), 3000)
      }
    } catch (err) {
      console.error(err)
    }
    setSaving(false)
  }

  if (loading) {
    return <AppLayout><div className="page-container"><div className="loading-card">Loading profile…</div></div></AppLayout>
  }

  return (
    <AppLayout>
      <div className="page-container">
        <div className="page-header">
          <div>
            <h1 className="page-title">My Profile</h1>
            <p className="page-subtitle">Your account information and queue history</p>
          </div>
        </div>

        <div className="profile-grid">
          <div className="card profile-card">
            <div className="profile-avatar">
              {(user?.fullName || 'U').charAt(0).toUpperCase()}
            </div>
            <h2 className="profile-name">{user?.fullName}</h2>
            <span className="nav-role-badge large">{user?.role}</span>

            <div className="profile-fields">
              <div className="profile-field">
                <label>Email</label>
                <span>{user?.email}</span>
              </div>
              <div className="profile-field">
                <label>Phone</label>
                <span>{user?.phoneNumber || 'Not set'}</span>
              </div>
              <div className="profile-field">
                <label>Role</label>
                <span>{user?.role}</span>
              </div>
            </div>
          </div>

          <div className="card history-card">
            {user?.role === 'DOCTOR' ? (
              <div>
                <h2 className="dash-card-title">Professional Information</h2>
                <div style={{ marginTop: '20px' }}>
                  <label style={{ display: 'block', fontWeight: 'bold', marginBottom: '8px' }}>Medical Specialty</label>
                  <select 
                    className="input" 
                    value={specialty} 
                    onChange={(e) => setSpecialty(e.target.value)}
                    style={{ marginBottom: '15px', width: '100%', padding: '10px' }}
                  >
                    <option value="">Select your specialty</option>
                    {specialties.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                  <button 
                    className="primaryBtn" 
                    onClick={handleUpdateSpecialty}
                    disabled={saving || !specialty}
                  >
                    {saving ? 'Saving...' : 'Save Specialty'}
                  </button>
                  {successMsg && <p style={{ color: '#10b981', marginTop: '10px' }}>{successMsg}</p>}
                </div>
              </div>
            ) : (
              <div>
                <h2 className="dash-card-title">Queue History</h2>
                {history.length === 0 ? (
                  <div className="sub">No queue history yet.</div>
                ) : (
                  <div className="history-list">
                    {history.slice(0, 20).map(entry => (
                      <div key={entry.id} className="history-row">
                        <div className="history-info">
                          <div className="history-clinic">{entry.clinicName}</div>
                          <div className="history-queue">{entry.queueName} — #{entry.queueNumber}</div>
                        </div>
                        <div className="history-right">
                          <span className={`status-badge ${entry.status.toLowerCase()}`}>{entry.status}</span>
                          <span className="history-time">{entry.checkInTime ? new Date(entry.checkInTime).toLocaleDateString() : ''}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  )
}
