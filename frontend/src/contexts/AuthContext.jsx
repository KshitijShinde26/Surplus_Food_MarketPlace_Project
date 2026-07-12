import { createContext, useContext, useMemo, useState } from 'react';
import * as authService from '../services/authService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const storedUser = localStorage.getItem('currentUser');
    return storedUser ? JSON.parse(storedUser) : null;
  });

  const value = useMemo(() => ({
    user,
    isAuthenticated: Boolean(user),
    async register(payload) {
      const response = await authService.register(payload);
      setUser(response.user);
      return response;
    },
    async login(payload) {
      const response = await authService.login(payload);
      setUser(response.user);
      return response;
    },
    async logout() {
      try {
        await authService.logout();
      } catch (e) {
        console.warn('Backend logout failed:', e);
      } finally {
        setUser(null);
      }
    },
  }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
