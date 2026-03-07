import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { clearToken } from '../lib/auth'

export default function DashboardPage() {
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let mounted = true
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const res = await api.get('/api/dashboard')
        if (mounted) setData(res.data)
      } catch (err) {
        const status = err && err.response && err.response.status
        if (status === 401) {
          clearToken()
          navigate('/login', { replace: true })
          return
        }
        if (mounted) setError('Failed to load dashboard.')
      } finally {
        if (mounted) setLoading(false)
      }
    }
    load()
    return () => {
      mounted = false
    }
  }, [navigate])

  function logout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  return (
    <div className="page">
      <div className="dashContainer">
        <div className="dashTop">
          <div>
            <div className="dashTitle">Dashboard</div>
            <div className="dashSub">Authenticated area</div>
          </div>
          <button onClick={logout} className="outlineBtn small">
            Logout
          </button>
        </div>

        <div className="card dashCard">
          {loading ? (
            <div className="sub">Loading…</div>
          ) : error ? (
            <div className="errorText">{error}</div>
          ) : (
            <div>
              <div className="dashMessage">{data && data.message}</div>
              {data && data.email ? <div className="sub">{data.email}</div> : null}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

