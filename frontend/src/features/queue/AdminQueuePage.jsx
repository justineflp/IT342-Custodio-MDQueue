import { useEffect, useState } from 'react'
import AppLayout from '../../shared/components/AppLayout'
import { getMyClinics, createClinic } from '../clinic/clinicApi'
import { getQueuesByClinic, createQueue, serveNext, completeEntry, getQueueEntries, updateQueueStatus } from './queueApi'

export default function AdminQueuePage() {
  const [clinics, setClinics] = useState([])
  const [selectedClinic, setSelectedClinic] = useState(null)
  const [queues, setQueues] = useState([])
  const [selectedQueue, setSelectedQueue] = useState(null)
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [message, setMessage] = useState(null)

  // Create forms
  const [showClinicForm, setShowClinicForm] = useState(false)
  const [clinicForm, setClinicForm] = useState({ name: '', address: '', phoneNumber: '', description: '', openingTime: '08:00', closingTime: '17:00' })
  const [showQueueForm, setShowQueueForm] = useState(false)
  const [queueName, setQueueName] = useState('')

  useEffect(() => { loadClinics() }, [])

  async function loadClinics() {
    setLoading(true)
    const result = await getMyClinics()
    if (result.success) setClinics(result.data || [])
    setLoading(false)
  }

  async function selectClinic(clinic) {
    setSelectedClinic(clinic)
    setSelectedQueue(null)
    setEntries([])
    const result = await getQueuesByClinic(clinic.id)
    if (result.success) setQueues(result.data || [])
  }

  async function selectQueue(queue) {
    setSelectedQueue(queue)
    const result = await getQueueEntries(queue.id)
    if (result.success) setEntries(result.data || [])
  }

  async function handleCreateClinic(e) {
    e.preventDefault()
    const result = await createClinic(clinicForm)
    if (result.success) {
      setShowClinicForm(false)
      setClinicForm({ name: '', address: '', phoneNumber: '', description: '', openingTime: '08:00', closingTime: '17:00' })
      loadClinics()
      setMessage({ type: 'success', text: 'Clinic created!' })
    } else {
      setMessage({ type: 'error', text: result.message })
    }
  }

  async function handleCreateQueue(e) {
    e.preventDefault()
    if (!selectedClinic) return
    const result = await createQueue(selectedClinic.id, { name: queueName })
    if (result.success) {
      setShowQueueForm(false)
      setQueueName('')
      selectClinic(selectedClinic)
      setMessage({ type: 'success', text: 'Queue created!' })
    } else {
      setMessage({ type: 'error', text: result.message })
    }
  }

  async function handleServeNext() {
    if (!selectedQueue) return
    const result = await serveNext(selectedQueue.id)
    if (result.success) {
      selectQueue(selectedQueue)
      setMessage({ type: 'success', text: `Now serving #${result.data.queueNumber} — ${result.data.patientName}` })
    } else {
      setMessage({ type: 'error', text: result.message })
    }
  }

  async function handleComplete(entryId) {
    const result = await completeEntry(entryId)
    if (result.success) {
      selectQueue(selectedQueue)
      setMessage({ type: 'success', text: 'Patient marked as complete' })
    } else {
      setMessage({ type: 'error', text: result.message })
    }
  }

  async function handleToggleQueue() {
    if (!selectedQueue) return
    const newStatus = selectedQueue.status === 'OPEN' ? 'CLOSED' : 'OPEN'
    const result = await updateQueueStatus(selectedQueue.id, newStatus)
    if (result.success) {
      setSelectedQueue({ ...selectedQueue, status: newStatus })
      selectClinic(selectedClinic)
      setMessage({ type: 'success', text: `Queue ${newStatus.toLowerCase()}` })
    }
  }

  return (
    <AppLayout>
      <div className="page-container">
        <div className="page-header">
          <div>
            <h1 className="page-title">Queue Management</h1>
            <p className="page-subtitle">Manage your clinics, queues, and serve patients</p>
          </div>
          <button className="primaryBtn small-btn" onClick={() => setShowClinicForm(!showClinicForm)}>
            + New Clinic
          </button>
        </div>

        {message && (
          <div className={`alert ${message.type === 'success' ? 'alert-success' : ''}`}>
            {message.text}
            <button className="alert-close" onClick={() => setMessage(null)}>×</button>
          </div>
        )}

        {showClinicForm && (
          <form onSubmit={handleCreateClinic} className="card admin-form">
            <h3>Create New Clinic</h3>
            <div className="form-row">
              <input className="input" placeholder="Clinic name *" value={clinicForm.name} onChange={e => setClinicForm({ ...clinicForm, name: e.target.value })} required />
              <input className="input" placeholder="Address" value={clinicForm.address} onChange={e => setClinicForm({ ...clinicForm, address: e.target.value })} />
            </div>
            <div className="form-row">
              <input className="input" placeholder="Phone number" value={clinicForm.phoneNumber} onChange={e => setClinicForm({ ...clinicForm, phoneNumber: e.target.value })} />
              <input className="input" type="time" value={clinicForm.openingTime} onChange={e => setClinicForm({ ...clinicForm, openingTime: e.target.value })} />
              <input className="input" type="time" value={clinicForm.closingTime} onChange={e => setClinicForm({ ...clinicForm, closingTime: e.target.value })} />
            </div>
            <textarea className="input textarea" placeholder="Description" value={clinicForm.description} onChange={e => setClinicForm({ ...clinicForm, description: e.target.value })} />
            <div className="form-actions">
              <button type="submit" className="primaryBtn small-btn">Create Clinic</button>
              <button type="button" className="outlineBtn small" onClick={() => setShowClinicForm(false)}>Cancel</button>
            </div>
          </form>
        )}

        <div className="admin-layout">
          {/* Clinics sidebar */}
          <div className="admin-sidebar">
            <h3 className="sidebar-title">My Clinics</h3>
            {loading ? <div className="sub">Loading…</div> :
              clinics.length === 0 ? <div className="sub">No clinics yet. Create one!</div> :
              clinics.map(clinic => (
                <button
                  key={clinic.id}
                  className={`sidebar-item ${selectedClinic?.id === clinic.id ? 'active' : ''}`}
                  onClick={() => selectClinic(clinic)}
                >
                  <span>{clinic.name}</span>
                  <span className="badge">{clinic.activeQueues}</span>
                </button>
              ))
            }
          </div>

          {/* Main content */}
          <div className="admin-main">
            {!selectedClinic ? (
              <div className="empty-state-card">Select a clinic from the sidebar to manage its queues.</div>
            ) : (
              <>
                <div className="admin-section-header">
                  <h2>{selectedClinic.name} — Queues</h2>
                  <button className="primaryBtn small-btn" onClick={() => setShowQueueForm(!showQueueForm)}>+ New Queue</button>
                </div>

                {showQueueForm && (
                  <form onSubmit={handleCreateQueue} className="inline-form">
                    <input className="input" placeholder="Queue name (e.g. General Consultation)" value={queueName} onChange={e => setQueueName(e.target.value)} required />
                    <button type="submit" className="primaryBtn small-btn">Create</button>
                  </form>
                )}

                <div className="queue-list">
                  {queues.map(queue => (
                    <button
                      key={queue.id}
                      className={`queue-card clickable ${selectedQueue?.id === queue.id ? 'selected' : ''}`}
                      onClick={() => selectQueue(queue)}
                    >
                      <div className="queue-card-left">
                        <h3 className="queue-card-name">{queue.name}</h3>
                        <div className="queue-card-stats">
                          <span className={`status-badge ${queue.status.toLowerCase()}`}>{queue.status}</span>
                          <span className="queue-stat">{queue.waitingCount} waiting</span>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>

                {selectedQueue && (
                  <div className="admin-queue-detail">
                    <div className="admin-section-header">
                      <h3>{selectedQueue.name} — Patients</h3>
                      <div className="admin-actions">
                        <button className="primaryBtn small-btn" onClick={handleServeNext}>Serve Next</button>
                        <button className={`outlineBtn small ${selectedQueue.status === 'OPEN' ? 'danger-text' : ''}`} onClick={handleToggleQueue}>
                          {selectedQueue.status === 'OPEN' ? 'Close Queue' : 'Open Queue'}
                        </button>
                      </div>
                    </div>

                    {entries.length === 0 ? (
                      <div className="sub">No entries in this queue.</div>
                    ) : (
                      <div className="entries-table">
                        <div className="table-header">
                          <span>#</span>
                          <span>Patient</span>
                          <span>Status</span>
                          <span>Check-in</span>
                          <span>Action</span>
                        </div>
                        {entries.map(entry => (
                          <div key={entry.id} className="table-row">
                            <span className="table-num">#{entry.queueNumber}</span>
                            <span>{entry.patientName}</span>
                            <span><span className={`status-badge ${entry.status.toLowerCase()}`}>{entry.status}</span></span>
                            <span className="table-time">{entry.checkInTime ? new Date(entry.checkInTime).toLocaleTimeString() : '—'}</span>
                            <span>
                              {entry.status === 'SERVING' && (
                                <button className="primaryBtn small-btn" onClick={() => handleComplete(entry.id)}>Complete</button>
                              )}
                            </span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </AppLayout>
  )
}
