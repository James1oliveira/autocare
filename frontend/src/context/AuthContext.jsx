import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { api } from '../api.js';

const AuthContext = createContext(null);
const STORAGE_KEY = 'autocare.session';

function loadSession() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY));
    if (!saved?.token) return null;
    // Drop the session if the JWT has expired
    const payload = JSON.parse(atob(saved.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    if (payload.exp && payload.exp * 1000 < Date.now()) return null;
    return saved;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession);

  const save = useCallback((data) => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
    } catch {
      // private browsing: the session just won't survive a refresh
    }
    setSession(data);
  }, []);

  const login = useCallback(async (email, password) => {
    const data = await api('/api/auth/login', { method: 'POST', body: { email, password } });
    save(data);
    return data.user;
  }, [save]);

  const register = useCallback(async (fullName, email, password) => {
    const data = await api('/api/auth/register', { method: 'POST', body: { fullName, email, password } });
    save(data);
    return data.user;
  }, [save]);

  const logout = useCallback(() => {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
    setSession(null);
  }, []);

  const value = useMemo(() => ({
    user: session?.user ?? null,
    token: session?.token ?? null,
    login,
    register,
    logout,
  }), [session, login, register, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}

export function homeFor(user) {
  if (!user) return '/';
  return user.role === 'CUSTOMER' ? '/my-car' : '/workshop';
}
