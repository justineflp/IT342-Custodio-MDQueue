import { useEffect, useState, useCallback } from 'react'
import AppLayout from '../../shared/components/AppLayout'
import { getMyActiveEntries, cancelEntry } from './queueApi'

export default function QueueStatusPage() {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)

  const loadEntries = useCallback(async () => {
    setLoading(true)
    const result = await getMyActiveEntries()
    if (result.success) setEntries(result.data || [])
    setLoading(false)
  }, [])

  useEffect(() => {
    loadEntries()
    const interval = setInterval(loadEntries, 10000) // Poll every 10s
    return () => clearInterval(interval)
  }, [loadEntries])

  async function handleCancel(entryId) {
    const result = await cancelEntry(entryId)
    if (result.success) loadEntries()
  }

  return (
    <AppLayout>
      <div className="page-container">
        <div className="page-header">
          <div>
            <h1 className="page-title">My Queue Status</h1>
            <p className="page-subtitle">Track your position in real-time (updates every 10s)</p>
          </div>
        </div>

        {loading && entries.length === 0 ? (
          <div className="loading-card">Loading your queue status…</div>
        ) : entries.length === 0 ? (
          <div className="empty-state-card">
            <div className="empty-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" width="48" height="48"><circle cx="12" cy="12" r="10" /><path d="M8 15h8M9 9h.01M15 9h.01" /></svg>
            </div>
            <p>You're not in any queues right now.</p>
          </div>
        ) : (
          <div className="queue-status-list">
            {entries.map(entry => (
              <div key={entry.id} className="queue-status-card">
                <div className="qs-header">
                  <div>
                    <h3 className="qs-clinic">{entry.clinicName}</h3>
                    <p className="qs-queue">{entry.queueName}</p>
                  </div>
                  <div className={`status-badge large ${entry.status.toLowerCase()}`}>
                    {entry.status}
                  </div>
                </div>

                <div className="qs-body">
                  <div className="qs-number-box">
                    <div className="qs-number-label">Your Number</div>
                    <div className="qs-number">#{entry.queueNumber}</div>
                  </div>

                  {entry.status === 'WAITING' && (
                    <div className="qs-position-box">
                      <div className="qs-position-label">People Ahead</div>
                      <div className="qs-position">{entry.peopleAhead}</div>
                    </div>
                  )}

                  {entry.status === 'SERVING' && (
                    <div className="qs-serving-box">
                      <div className="qs-serving-text">🎉 It's your turn!</div>
                      <div className="qs-serving-sub">Please proceed to the clinic</div>
                    </div>
                  )}
                </div>

                {entry.status === 'WAITING' && (
                  <div className="qs-footer">
                    <button className="outlineBtn small danger-text" onClick={() => handleCancel(entry.id)}>
                      Cancel
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  )
}
