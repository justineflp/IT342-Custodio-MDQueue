export type AuthResponse = {
  token: string
  type: string
  userId: number
  email: string
  fullName: string
  message?: string
}

export type ApiErrorBody = {
  message?: string
  error?: string
  status?: number
  errors?: Record<string, string>
}

