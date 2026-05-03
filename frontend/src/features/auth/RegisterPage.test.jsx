import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import RegisterPage from './RegisterPage'

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

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  function renderRegister() {
    return render(
      <MemoryRouter initialEntries={['/register']}>
        <RegisterPage />
      </MemoryRouter>
    )
  }

  it('FR-09: renders registration form with all required fields', () => {
    renderRegister()

    expect(screen.getByText('Create an account')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('John Doe')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('you@example.com')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('+1 (555) 000-0000')).toBeInTheDocument()
    expect(screen.getAllByPlaceholderText('••••••••')).toHaveLength(2)
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument()
  })

  it('FR-09: renders Sign In link to login page', () => {
    renderRegister()

    const signInLink = screen.getByText('Sign in')
    expect(signInLink).toBeInTheDocument()
    expect(signInLink.closest('a')).toHaveAttribute('href', '/login')
  })

  it('FR-01: shows error message on failed registration', async () => {
    const user = userEvent.setup()
    api.post.mockRejectedValueOnce({
      response: { data: { message: 'An account with this email already exists' } },
    })

    renderRegister()

    await user.type(screen.getByPlaceholderText('John Doe'), 'Test User')
    await user.type(screen.getByPlaceholderText('you@example.com'), 'test@test.com')
    const passwordFields = screen.getAllByPlaceholderText('••••••••')
    await user.type(passwordFields[0], 'password123')
    await user.type(passwordFields[1], 'password123')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(screen.getByText('An account with this email already exists')).toBeInTheDocument()
    })
  })

  it('FR-01: calls API and stores token on successful registration', async () => {
    const user = userEvent.setup()
    api.post.mockResolvedValueOnce({
      data: { token: 'new-jwt-token', type: 'Bearer' },
    })

    renderRegister()

    await user.type(screen.getByPlaceholderText('John Doe'), 'Test User')
    await user.type(screen.getByPlaceholderText('you@example.com'), 'test@test.com')
    const passwordFields = screen.getAllByPlaceholderText('••••••••')
    await user.type(passwordFields[0], 'password123')
    await user.type(passwordFields[1], 'password123')
    await user.click(screen.getByRole('button', { name: /create account/i }))

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/api/auth/register', {
        fullName: 'Test User',
        email: 'test@test.com',
        phoneNumber: null,
        password: 'password123',
        confirmPassword: 'password123',
      })
      expect(setToken).toHaveBeenCalledWith('new-jwt-token')
    })
  })
})
