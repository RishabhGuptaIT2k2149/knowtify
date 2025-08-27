import { create } from 'zustand'
import { authAPI } from '../services/api'

export const useAuthStore = create((set, get) => ({
  // State
  user: null,
  accessToken: localStorage.getItem('accessToken'),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  isLoading: false,
  error: null,

  // Actions
  login: async (credentials) => {
    set({ isLoading: true, error: null })
    try {
      const response = await authAPI.login(credentials)

      // Accept common token field names
      const token =
        response?.data?.token ||
        response?.data?.accessToken ||
        response?.data?.jwt

      const message = response?.data?.message

      if (!token) {
        const keys = response?.data ? Object.keys(response.data) : []
        throw new Error(
          `No token received from server${keys.length ? ` (keys: ${keys.join(', ')})` : ''}`
        )
      }

      // Persist token
      localStorage.setItem('accessToken', token)

      set({
        accessToken: token,
        isAuthenticated: true,
        isLoading: false,
        user: { username: credentials.username },
        error: null,
      })

      return { success: true, message }
    } catch (error) {
      const errorMessage =
        error?.response?.data?.message ||
        error?.message ||
        'Login failed'
      set({ error: errorMessage, isLoading: false, isAuthenticated: false })
      return { success: false, error: errorMessage }
    }
  },

  register: async (userData) => {
    set({ isLoading: true, error: null })
    try {
      const response = await authAPI.register(userData)
      set({ isLoading: false })
      return { success: true, message: response?.data?.message }
    } catch (error) {
      const errorMessage =
        error?.response?.data?.message || 'Registration failed'
      set({ error: errorMessage, isLoading: false })
      return { success: false, error: errorMessage }
    }
  },

  logout: () => {
    localStorage.removeItem('accessToken')
    set({
      user: null,
      accessToken: null,
      isAuthenticated: false,
      error: null,
    })
  },

  clearError: () => set({ error: null }),
}))
