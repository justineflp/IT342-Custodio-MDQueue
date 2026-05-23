import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import AppLayout from '../../shared/components/AppLayout'
import { getClinics } from './clinicApi'

export default function ClinicsPage() {
  const [clinics, setClinics] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadClinics()
  }, [])

  async function loadClinics(query) {
    setLoading(true)
    const result = await getClinics(query)
    if (result.success) setClinics(result.data || [])
    setLoading(false)
  }

  function onSearch(e) {
    e.preventDefault()
    loadClinics(search)
  }

  return (
    <AppLayout>
      <div className="page-container">
        <div className="page-header">
          <div>
            <h1 className="page-title">Clinics</h1>
            <p className="page-subtitle">Browse medical clinics and join a queue</p>
          </div>
        </div>

        <form onSubmit={onSearch} className="search-bar">
          <input
            type="text"
            className="search-input"
            placeholder="Search clinics by name…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <button type="submit" className="search-btn">Search</button>
        </form>

        {loading ? (
          <div className="loading-card">Loading clinics…</div>
        ) : clinics.length === 0 ? (
          <div className="empty-state-card">
            <p>No clinics found.</p>
          </div>
        ) : (
          <div className="clinic-grid">
            {clinics.map(clinic => (
              <Link key={clinic.id} to={`/clinics/${clinic.id}`} className="clinic-card">
                <div className="clinic-card-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="28" height="28"><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"/><path d="M9 22V12h6v10"/></svg>
                </div>
                <h3 className="clinic-card-name">{clinic.name}</h3>
                {clinic.address && <p className="clinic-card-addr">{clinic.address}</p>}
                <div className="clinic-card-footer">
                  <span className="clinic-card-queues">{clinic.activeQueues} active queue{clinic.activeQueues !== 1 ? 's' : ''}</span>
                  <span className="clinic-card-owner">Dr. {clinic.ownerName}</span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </AppLayout>
  )
}
