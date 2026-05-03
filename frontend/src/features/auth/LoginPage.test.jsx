import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import LoginPage from './LoginPage'

// Mock the api module
vi.mock('../../shared/lib/api', () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
    interceptors: { request: { use: vi.fn() } },
  },
}))

// Mock the auth module
vi.mock('./lib/auth', () => ({
  setToken: vi.fn(),
  getToken: vi.fn(() => null),
  clearToken: vi.fn(),
  isLoggedIn: vi.fn(() => false),
}))

import { api } from '../../shared/lib/api'
import { setToken } from './lib/auth'

describe('LoginPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  function renderLogin() {
    return render(
      <MemoryRouter initialEntries={['/login']}>
        <LoginPage />
      </MemoryRouter>
    )
  }

  function getSignInButton() {
    return screen.getByRole('button', { name: 'Sign In' })
  }

  it('FR-09: renders login form with email and password fields', () => {
    renderLogin()

    expect(screen.getByText('Welcome back')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('••••••••')).toBeInTheDocument()
    expect(getSignInButton()).toBeInTheDocument()
  })

  it('FR-09: renders Sign Up link to register page', () => {
    renderLogin()

    const signUpLink = screen.getByText('Sign up')
    expect(signUpLink).toBeInTheDocument()
    expect(signUpLink.closest('a')).toHaveAttribute('href', '/register')
  })

  it('FR-09: renders Remember me checkbox and Forgot password button', () => {
    renderLogin()

    expect(screen.getByText('Remember me')).toBeInTheDocument()
    expect(screen.getByText('Forgot password?')).toBeInTheDocument()
  })

  it('FR-04: shows error message on failed login', async () => {
    const user = userEvent.setup()
    api.post.mockRejectedValueOnce({
      response: { data: { message: 'Invalid email or password' } },
    })

    renderLogin()

    await user.type(screen.getByPlaceholderText('you@example.com'), 'test@test.com')
    await user.type(screen.getByPlaceholderText('••••••••'), 'wrongpass')
    await user.click(getSignInButton())

    await waitFor(() => {
      expect(screen.getByText('Invalid email or password')).toBeInTheDocument()
    })
  })

  it('FR-04: calls API and stores token on successful login', async () => {
    const user = userEvent.setup()
    api.post.mockResolvedValueOnce({
      data: { token: 'test-jwt-token', type: 'Bearer' },
    })

    renderLogin()

    await user.type(screen.getByPlaceholderText('you@example.com'), 'test@test.com')
    await user.type(screen.getByPlaceholderText('••••••••'), 'password123')
    await user.click(getSignInButton())

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/login', {
        email: 'test@test.com',
        password: 'password123',
      })
      expect(setToken).toHaveBeenCalledWith('test-jwt-token')
    })
  })

  it('FR-04: disables submit button while submitting', async () => {
    const user = userEvent.setup()
    let resolvePromise
    api.post.mockReturnValueOnce(new Promise((resolve) => { resolvePromise = resolve }))

    renderLogin()

    await user.type(screen.getByPlaceholderText('you@example.com'), 'test@test.com')
    await user.type(screen.getByPlaceholderText('••••••••'), 'password123')
    await user.click(getSignInButton())

    expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled()

    resolvePromise({ data: { token: 'tok' } })
  })
})
