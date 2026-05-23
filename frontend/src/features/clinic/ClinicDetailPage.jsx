import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getClinic } from './clinicApi'
import { getQueuesByClinic, joinQueue } from '../queue/queueApi'

export default function ClinicDetailPage() {
  const { id } = useParams()
  const [clinic, setClinic] = useState(null)
  const [queues, setQueues] = useState([])
  const [loading, setLoading] = useState(true)
  const [joining, setJoining] = useState(null)
  const [message, setMessage] = useState(null)

  useEffect(() => {
    loadData()
  }, [id])

  async function loadData() {
    setLoading(true)
    const [clinicRes, queuesRes] = await Promise.all([
      getClinic(id),
      getQueuesByClinic(id)
    ])
    if (clinicRes.success) setClinic(clinicRes.data)
    if (queuesRes.success) setQueues(queuesRes.data || [])
    setLoading(false)
  }

  async function handleJoin(queueId) {
    setJoining(queueId)
    setMessage(null)
    const result = await joinQueue(queueId)
    if (result.success) {
      setMessage({ type: 'success', text: `Joined queue! Your number is #${result.data.queueNumber}` })
      loadData()
    } else {
      setMessage({ type: 'error', text: result.message })
    }
    setJoining(null)
  }

  if (loading) {
    return <AppLayout><div className="page-container"><div className="loading-card">Loading clinic…</div></div></AppLayout>
  }

  if (!clinic) {
    return <AppLayout><div className="page-container"><div className="empty-state-card">Clinic not found.</div></div></AppLayout>
  }

  return (
    <AppLayout>
      <div className="page-container">
        <div className="clinic-detail-header">
          <div className="clinic-detail-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="32" height="32"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path d="M9 22V12h6v10"/></svg>
          </div>
          <div>
            <h1 className="page-title">{clinic.name}</h1>
            {clinic.address && <p className="page-subtitle">{clinic.address}</p>}
          </div>
        </div>

        <div className="clinic-info-grid">
          {clinic.phoneNumber && (
            <div className="info-chip">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
              {clinic.phoneNumber}
            </div>
          )}
          {clinic.openingTime && (
            <div className="info-chip">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>
              {clinic.openingTime} – {clinic.closingTime}
            </div>
          )}
          <div className="info-chip">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            Dr. {clinic.ownerName}
          </div>
        </div>

        {clinic.description && (
          <div className="clinic-description card">{clinic.description}</div>
        )}

        {message && (
          <div className={`alert ${message.type === 'success' ? 'alert-success' : ''}`}>
            {message.text}
          </div>
        )}

        <h2 className="section-title">Available Queues</h2>

        {queues.length === 0 ? (
          <div className="empty-state-card">No queues available at this clinic.</div>
        ) : (
          <div className="queue-list">
            {queues.map(queue => (
              <div key={queue.id} className="queue-card">
                <div className="queue-card-left">
                  <h3 className="queue-card-name">{queue.name}</h3>
                  <div className="queue-card-stats">
                    <span className={`status-badge ${queue.status.toLowerCase()}`}>{queue.status}</span>
                    <span className="queue-stat">{queue.waitingCount} waiting</span>
                    <span className="queue-stat">Current: #{queue.currentNumber}</span>
                  </div>
                </div>
                <button
                  className="primaryBtn small-btn"
                  disabled={queue.status !== 'OPEN' || joining === queue.id}
                  onClick={() => handleJoin(queue.id)}
                >
                  {joining === queue.id ? 'Joining…' : queue.status === 'OPEN' ? 'Join Queue' : 'Closed'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  )
}
