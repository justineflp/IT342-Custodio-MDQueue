import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../components/AuthLayout'
import InputField from '../components/InputField'
import { api } from '../lib/api'
import { setToken } from '../lib/auth'
import type { ApiErrorBody, AuthResponse } from '../lib/types'

export default function RegisterPage() {
  const navigate = useNavigate()

  const [fullName, setFullName] = useState('')
  const [email, setEmail] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError(null)
    setFieldErrors({})
    setSubmitting(true)
    try {
      const res = await api.post<AuthResponse>('/api/auth/register', {
        fullName,
        email,
        phoneNumber: phoneNumber || null,
        password,
        confirmPassword,
      })
      setToken(res.data.token)
      navigate('/dashboard', { replace: true })
    } catch (err: any) {
      const data: ApiErrorBody | undefined = err?.response?.data
      setError(data?.message || 'Registration failed. Please try again.')
      if (data?.errors) setFieldErrors(data.errors)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout>
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-slate-900">Create an account</h1>
        <p className="text-sm text-slate-500">Get started with MDQueue today</p>
      </div>

      <form onSubmit={onSubmit} className="mt-6 space-y-4">
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

        {error ? (
          <div className="rounded-xl border border-red-100 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        ) : null}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-xl bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm hover:bg-blue-700 disabled:opacity-60"
        >
          {submitting ? 'Creating…' : 'Create Account'}
        </button>

        <div className="pt-1 text-center text-sm text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="font-medium text-blue-600 hover:text-blue-700">
            Sign in
          </Link>
        </div>
      </form>
    </AuthLayout>
  )
}

