import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import InputField from '../components/InputField'
import { authFacade } from '../lib/authFacade'

export default function RegisterPage() {
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})

  async function onSubmit(e) {
    e.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    try {
      // Facade Pattern: single call replaces api.post() + setToken() + error parsing
      const result = await authFacade.register({
        fullName,
        email,
        phoneNumber: phoneNumber || null,
        password,
        confirmPassword,
      })
      if (result.success) {
        navigate('/dashboard', { replace: true })
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
        <h1 className="h1">Create an account</h1>
        <p className="sub">Get started with MDQueue today</p>
      </div>

      <form onSubmit={onSubmit} className="form">
        <InputField
          label="Full Name"
          placeholder="John Doe"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          required
          autoComplete="name"
          error={fieldErrors.fullName}
        />

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
          label="Phone Number"
          placeholder="+1 (555) 000-0000"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          autoComplete="tel"
          error={fieldErrors.phoneNumber}
        />

        <InputField
          label="Password"
          type="password"
          placeholder="••••••••"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          autoComplete="new-password"
          error={fieldErrors.password}
        />

        <InputField
          label="Confirm Password"
          type="password"
          placeholder="••••••••"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
          autoComplete="new-password"
          error={fieldErrors.confirmPassword}
        />

        {error ? <div className="alert">{error}</div> : null}

        <button type="submit" disabled={submitting} className="primaryBtn">
          {submitting ? 'Creating…' : 'Create Account'}
        </button>

        <div className="bottomText">
          Already have an account? <Link className="link" to="/login">Sign in</Link>
        </div>
      </form>
    </AuthLayout>
  )
}

