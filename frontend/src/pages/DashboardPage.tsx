import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api } from '../lib/api'
import { clearToken } from '../lib/auth'

type DashboardResponse = {
  message: string
  email?: string
}

export default function DashboardPage() {
  const navigate = useNavigate()
  const [data, setData] = useState<DashboardResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let mounted = true
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const res = await api.get<DashboardResponse>('/api/dashboard')
        if (mounted) setData(res.data)
      } catch (err: any) {
        const status = err?.response?.status
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
    void load()
    return () => {
      mounted = false
    }
  }, [navigate])

  function logout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  return (
    <div className="min-h-full bg-slate-50">
      <div className="mx-auto max-w-2xl px-4 py-10">
        <div className="flex items-center justify-between">
          <div>
            <div className="text-lg font-semibold text-slate-900">Dashboard</div>
            <div className="text-sm text-slate-500">Authenticated area</div>
          </div>
          <button
            onClick={logout}
            className="rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
          >
            Logout
          </button>
        </div>

        <div className="mt-6 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200">
          {loading ? (
            <div className="text-sm text-slate-600">Loading…</div>
          ) : error ? (
            <div className="text-sm text-red-700">{error}</div>
          ) : (
            <div className="space-y-2">
              <div className="text-base font-semibold text-slate-900">{data?.message}</div>
              {data?.email ? <div className="text-sm text-slate-600">{data.email}</div> : null}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

