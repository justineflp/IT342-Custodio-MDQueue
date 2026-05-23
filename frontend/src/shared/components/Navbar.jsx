import { Link, useLocation, useNavigate } from 'react-router-dom'
import { clearToken } from '../../features/auth/lib/auth'

export default function Navbar({ user }) {
  const navigate = useNavigate()
  const location = useLocation()

  function logout() {
    clearToken()
    navigate('/login', { replace: true })
  }

  const isActive = (path) => location.pathname === path ? 'nav-link active' : 'nav-link'
  const role = user?.role || 'PATIENT'

  return (
    <nav className="navbar">
      <div className="nav-inner">
        <Link to="/dashboard" className="nav-brand">
          <div className="nav-logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="22" height="22">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
            </svg>
          </div>
          <span className="nav-brand-text">MDQueue</span>
        </Link>

        <div className="nav-links">
          <Link to="/dashboard" className={isActive('/dashboard')}>Dashboard</Link>
          <Link to="/my-appointments" className={isActive('/my-appointments')}>Appointments</Link>
          {role === 'PATIENT' && (
            <Link to="/book-appointment" className={isActive('/book-appointment')}>Book Now</Link>
          )}
          {role === 'ADMIN' && (
            <Link to="/doctors" className={isActive('/doctors')}>Doctors</Link>
          )}
          <Link to="/profile" className={isActive('/profile')}>Profile</Link>
        </div>

        <div className="nav-right">
          <div className="nav-user">
            <span className="nav-user-name">{user?.fullName || 'User'}</span>
            <span className="nav-role-badge">{role}</span>
          </div>
          <button onClick={logout} className="nav-logout-btn">Logout</button>
        </div>
      </div>
    </nav>
  )
}
