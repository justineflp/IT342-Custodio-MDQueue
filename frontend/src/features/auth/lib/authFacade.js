/**
 * Facade Pattern: Simplified interface for authentication operations.
 *
 * This module acts as a FACADE that encapsulates the complex interactions
 * between multiple subsystems:
 *   - API client (api.js) — handles HTTP requests
 *   - Auth token manager (auth.js) — handles localStorage token operations
 *   - Error formatting — normalizes API error responses
 *
 * Instead of each page component (LoginPage, RegisterPage) directly coordinating
 * these subsystems, they call simple facade methods like `authFacade.login()`
 * and receive a clean, predictable result object.
 *
 * Benefits:
 *   - Components don't need to know about token storage mechanics
 *   - API endpoint changes only need updating in one place
 *   - Error handling is centralized and consistent
 *   - Testing is simplified (mock the facade instead of multiple subsystems)
 */

import { api } from '../../../shared/lib/api'
import { setToken } from './auth'

/**
 * Normalizes error responses from the API into a consistent format.
 *
 * @param {Error} err - The caught error from an API call
 * @returns {{ message: string, errors: object }} Normalized error info
 */
function extractError(err) {
  const data = err && err.response && err.response.data
  return {
    message: (data && data.message) || 'Something went wrong. Please try again.',
    errors: (data && (data.errors || (data.data))) || {},
  }
}

/**
 * Facade method for user login.
 * Coordinates: API call → token storage → result formatting.
 *
 * @param {string} email - User's email address
 * @param {string} password - User's password
 * @returns {Promise<{ success: boolean, data?: object, message?: string, errors?: object }>}
 */
export async function login(email, password) {
  try {
    const res = await api.post('/api/auth/login', { email, password })
    const payload = res.data.data || res.data
    setToken(payload.token)
    return {
      success: true,
      data: payload,
    }
  } catch (err) {
    const { message, errors } = extractError(err)
    return {
      success: false,
      message,
      errors,
    }
  }
}

/**
 * Facade method for user registration.
 * Coordinates: API call → token storage → result formatting.
 *
 * @param {{ fullName: string, email: string, phoneNumber?: string, password: string, confirmPassword: string }} data
 * @returns {Promise<{ success: boolean, data?: object, message?: string, errors?: object }>}
 */
export async function register(data) {
  try {
    const res = await api.post('/api/auth/register', data)
    const payload = res.data.data || res.data
    setToken(payload.token)
    return {
      success: true,
      data: payload,
    }
  } catch (err) {
    const { message, errors } = extractError(err)
    return {
      success: false,
      message,
      errors,
    }
  }
}

/**
 * Facade method for Google Login.
 * Coordinates: API call → token storage → result formatting.
 *
 * @param {string} googleToken - The Google ID Token JWT or mock token
 * @returns {Promise<{ success: boolean, data?: object, message?: string, errors?: object }>}
 */
export async function loginWithGoogle(googleToken) {
  try {
    const res = await api.post('/api/auth/google', { token: googleToken })
    const payload = res.data.data || res.data
    setToken(payload.token)
    return {
      success: true,
      data: payload,
    }
  } catch (err) {
    const { message, errors } = extractError(err)
    return {
      success: false,
      message,
      errors,
    }
  }
}

/**
 * The authFacade object — the single entry point for all auth operations.
 */
export const authFacade = {
  login,
  register,
  loginWithGoogle,
}
