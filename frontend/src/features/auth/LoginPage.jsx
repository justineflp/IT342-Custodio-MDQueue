import { useMemo, useState, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from './components/AuthLayout'
import InputField from './components/InputField'
import { authFacade } from './lib/authFacade'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const redirectTo = useMemo(() => (location.state && location.state.from) || '/dashboard', [location.state])

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})

  // Google OAuth States
  const [googleInitialized, setGoogleInitialized] = useState(false)
  const [showSimulate, setShowSimulate] = useState(false)
  const [simEmail, setSimEmail] = useState('')
  const [simulating, setSimulating] = useState(false)
  const [simError, setSimError] = useState(null)

  // Handle successful Google token authentication
  async function handleGoogleLoginSuccess(response) {
    setError(null)
    setSubmitting(true)
    try {
      const result = await authFacade.loginWithGoogle(response.credential)
      if (result.success) {
        navigate(redirectTo, { replace: true })
      } else {
        setError(result.message)
      }
    } catch (err) {
      setError('Google Sign-in failed. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  // Handle local development simulation login
  async function handleSimulateLogin(e) {
    e.preventDefault()
    if (!simEmail || !simEmail.includes('@')) {
      setSimError('Please enter a valid email address.')
      return
    }
    setSimError(null)
    setSimulating(true)
    try {
      const mockToken = `mock_google_token_${simEmail}`
      const result = await authFacade.loginWithGoogle(mockToken)
      if (result.success) {
        navigate(redirectTo, { replace: true })
      } else {
        setSimError(result.message)
      }
    } catch (err) {
      setSimError('Simulation failed. Please verify the backend is running.')
    } finally {
      setSimulating(false)
    }
  }

  // Dynamically load Google Identity Services script
  useEffect(() => {
    const id = 'google-gsi-client'
    if (document.getElementById(id)) {
      setGoogleInitialized(true)
      return
    }

    const script = document.createElement('script')
    script.id = id
    script.src = 'https://accounts.google.com/gsi/client'
    script.async = true
    script.defer = true
    script.onload = () => {
      setGoogleInitialized(true)
    }
    script.onerror = () => {
      console.warn('Failed to load Google Sign-In SDK.')
    }
    document.body.appendChild(script)
  }, [])

  // Initialize Google Accounts Identity and render standard Sign-in button
  useEffect(() => {
    if (!googleInitialized) return

    try {
      /* global google */
      google.accounts.id.initialize({
        client_id: import.meta.env.VITE_GOOGLE_CLIENT_ID || '1039485732948-dummyexampleid.apps.googleusercontent.com',
        callback: handleGoogleLoginSuccess,
        auto_select: false,
      })

      google.accounts.id.renderButton(
        document.getElementById('google-signin-btn'),
        { 
          theme: 'outline', 
          size: 'large', 
          width: '100%',
          text: 'signin_with',
          shape: 'rectangular'
        }
      )
    } catch (err) {
      console.error('Error rendering Google Sign-In button:', err)
    }
  }, [googleInitialized])

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    try {
      // Facade Pattern: single call replaces api.post() + setToken() + error parsing
      const result = await authFacade.login(email, password)
      if (result.success) {
        void remember
        navigate(redirectTo, { replace: true })
      } else {
        setError(result.message)
        if (result.errors) setFieldErrors(result.errors)
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout>
      <div className="header">
        <h1 className="h1">Welcome back</h1>
        <p className="sub">Sign in to your account to continue</p>
      </div>

      <form onSubmit={onSubmit} className="form">
        <InputField
          label="Email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          autoComplete="email"
          error={fieldErrors.email}
        />

        <InputField
          label="Password"
          type="password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          autoComplete="current-password"
          error={fieldErrors.password}
        />

        <div className="rowBetween">
          <label className="checkbox">
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
            />
            Remember me
          </label>
          <button type="button" className="linkButton">
            Forgot password?
          </button>
        </div>

        {error ? <div className="alert">{error}</div> : null}

        <button type="submit" disabled={submitting} className="primaryBtn">
          {submitting ? 'Signing in…' : 'Sign In'}
        </button>

        <div className="divider">
          <div className="line" />
          <div className="dividerText">Or continue with</div>
          <div className="line" />
        </div>

        {/* Render standard native Google Sign-In iframe-based button */}
        <div id="google-signin-btn" style={{ width: '100%', minHeight: '40px', display: 'flex', justifyContent: 'center' }} />

        {/* Developer simulation controls */}
        <div style={{ display: 'flex', flexDirection: 'column', width: '100%', alignItems: 'center', marginTop: '6px' }}>
          <button
            type="button"
            className="linkButton"
            style={{ fontSize: '0.85rem', textDecoration: 'underline', color: 'var(--primary-light, #3b82f6)' }}
            onClick={() => setShowSimulate(prev => !prev)}
          >
            {showSimulate ? 'Hide Developer Simulation Panel' : 'Or Simulate Google Login (Dev Mode)'}
          </button>

          {showSimulate && (
            <div style={{
              width: '100%',
              marginTop: '12px',
              padding: '16px',
              background: 'rgba(255, 255, 255, 0.05)',
              border: '1px dashed rgba(255, 255, 255, 0.15)',
              borderRadius: '8px',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
              boxSizing: 'border-box'
            }}>
              <div style={{ fontSize: '0.85rem', color: '#9ca3af', fontWeight: '500', textAlign: 'left' }}>
                🔧 Developer Simulation Mode
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="email"
                  placeholder="developer@example.com"
                  value={simEmail}
                  onChange={(e) => setSimEmail(e.target.value)}
                  style={{
                    flex: 1,
                    padding: '8px 12px',
                    borderRadius: '6px',
                    border: '1px solid rgba(255, 255, 255, 0.15)',
                    background: 'rgba(0, 0, 0, 0.2)',
                    color: '#fff',
                    fontSize: '0.9rem',
                    minWidth: '0'
                  }}
                />
                <button
                  type="button"
                  onClick={handleSimulateLogin}
                  disabled={simulating}
                  className="primaryBtn"
                  style={{
                    padding: '8px 16px',
                    fontSize: '0.9rem',
                    width: 'auto',
                    marginTop: 0,
                    whiteSpace: 'nowrap'
                  }}
                >
                  {simulating ? 'Go…' : 'Log In'}
                </button>
              </div>
              {simError && <div style={{ fontSize: '0.8rem', color: '#f87171', textAlign: 'left' }}>{simError}</div>}
            </div>
          )}
        </div>

        <div className="bottomText">
          Don&apos;t have an account? <Link className="link" to="/register">Sign up</Link>
        </div>
      </form>
    </AuthLayout>
  )
}

