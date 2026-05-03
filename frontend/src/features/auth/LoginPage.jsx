import { useMemo, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthLayout from './components/AuthLayout'
import InputField from './components/InputField'
import { api } from '../../shared/lib/api'
import { setToken } from './lib/auth'

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

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    try {
      const res = await api.post('/api/auth/login', { email, password })
      setToken(res.data.token)
      void remember
      navigate(redirectTo, { replace: true })
    } catch (err) {
      const data = err && err.response && err.response.data
      setError((data && data.message) || 'Login failed. Please try again.')
      if (data && data.errors) setFieldErrors(data.errors)
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

        <button type="button" className="outlineBtn">
          <svg width="18" height="18" viewBox="0 0 48 48" aria-hidden="true">
            <path
              fill="#FFC107"
              d="M43.6 20.5H42V20H24v8h11.3C33.7 32.7 29.3 36 24 36c-6.6 0-12-5.4-12-12s5.4-12 12-12c3 0 5.8 1.1 7.9 3l5.7-5.7C34.7 6.1 29.6 4 24 4 12.9 4 4 12.9 4 24s8.9 20 20 20 20-8.9 20-20c0-1.1-.1-2.2-.4-3.5Z"
            />
            <path
              fill="#FF3D00"
              d="M6.3 14.7 12.9 19.6C14.7 15.1 19 12 24 12c3 0 5.8 1.1 7.9 3l5.7-5.7C34.7 6.1 29.6 4 24 4 16.3 4 9.7 8.3 6.3 14.7Z"
            />
            <path
              fill="#4CAF50"
              d="M24 44c5.5 0 10.5-2 14.3-5.3l-6.6-5.6C29.7 34.9 27 36 24 36c-5.3 0-9.7-3.3-11.3-8l-6.6 5.1C9.5 39.7 16.2 44 24 44Z"
            />
            <path
              fill="#1976D2"
              d="M43.6 20.5H42V20H24v8h11.3c-1 2.7-3 5-5.6 6.5l.1.1 6.6 5.6C39 37.7 44 32.9 44 24c0-1.1-.1-2.2-.4-3.5Z"
            />
          </svg>
          Sign in with Google
        </button>

        <div className="bottomText">
          Don&apos;t have an account? <Link className="link" to="/register">Sign up</Link>
        </div>
      </form>
    </AuthLayout>
  )
}
