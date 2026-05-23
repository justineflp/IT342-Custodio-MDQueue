import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Navbar from './Navbar'
import { getUserProfile } from '../../features/queue/queueApi'
import { clearToken } from '../../features/auth/lib/auth'

export default function AppLayout({ children }) {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)

  useEffect(() => {
    async function load() {
      const result = await getUserProfile()
      if (result.success) {
        setUser(result.data)
      } else {
        clearToken()
        navigate('/login', { replace: true })
      }
    }
    load()
  }, [navigate])

  return (
    <div className="app-layout">
      <Navbar user={user} />
      <main className="app-main">
        {children}
      </main>
    </div>
  )
}
