import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'

// Mock the auth module
vi.mock('../../features/auth/lib/auth', () => ({
  isLoggedIn: vi.fn(),
  getToken: vi.fn(),
  setToken: vi.fn(),
  clearToken: vi.fn(),
}))

import { isLoggedIn } from '../../features/auth/lib/auth'

function renderWithRouter(loggedIn) {
  isLoggedIn.mockReturnValue(loggedIn)

  return render(
    <MemoryRouter initialEntries={['/protected']}>
      <Routes>
        <Route path="/login" element={<div>Login Page</div>} />
        <Route
          path="/protected"
          element={
            <ProtectedRoute>
              <div>Protected Content</div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  it('FR-09: renders children when user is authenticated', () => {
    renderWithRouter(true)
    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })

  it('FR-09: redirects to login when user is not authenticated', () => {
    renderWithRouter(false)
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
    expect(screen.getByText('Login Page')).toBeInTheDocument()
  })
})
