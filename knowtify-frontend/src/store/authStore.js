import { create } from 'zustand';
import { authAPI } from '../services/api';

export const useAuthStore = create((set, get) => ({
  // State
  user: null,
  accessToken: localStorage.getItem('accessToken'),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  isLoading: false,
  error: null,

  // Actions
  login: async (credentials) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authAPI.login(credentials);
      const { accessToken, message } = response.data;
      
      localStorage.setItem('accessToken', accessToken);
      set({ 
        accessToken, 
        isAuthenticated: true, 
        isLoading: false,
        user: { username: credentials.username } // Basic user info
      });
      
      return { success: true, message };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Login failed';
      set({ error: errorMessage, isLoading: false });
      return { success: false, error: errorMessage };
    }
  },

  register: async (userData) => {
    set({ isLoading: true, error: null });
    try {
      const response = await authAPI.register(userData);
      set({ isLoading: false });
      return { success: true, message: response.data.message };
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Registration failed';
      set({ error: errorMessage, isLoading: false });
      return { success: false, error: errorMessage };
    }
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    set({ 
      user: null, 
      accessToken: null, 
      isAuthenticated: false,
      error: null 
    });
  },

  clearError: () => set({ error: null }),
}));
