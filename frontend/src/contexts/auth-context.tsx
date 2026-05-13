'use client';

import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from 'react';
import type { User, UserRole } from '@/src/lib/mock-data';
import { getAccessToken } from '@/src/lib/api-client';
import {
  type AuthResult,
  getCurrentUser,
  login as loginService,
  loginWithRole as loginWithRoleService,
  logout as logoutService,
} from '@/src/lib/auth-api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<AuthResult>;
  loginWithRole: (role: UserRole) => Promise<AuthResult>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function hydrateUser() {
      if (!getAccessToken()) {
        setIsLoading(false);
        return;
      }

      const currentUser = await getCurrentUser();
      if (active) {
        setUser(currentUser);
        setIsLoading(false);
      }
    }

    hydrateUser();

    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    try {
      const result = await loginService({ email, password });
      if (result.success && result.user) {
        setUser(result.user);
        return result;
      }
      return { success: false, error: result.error };
    } finally {
      setIsLoading(false);
    }
  }, []);

  const loginWithRole = useCallback(async (role: UserRole) => {
    setIsLoading(true);
    try {
      const result = await loginWithRoleService(role);
      if (result.success && result.user) {
        setUser(result.user);
        return result;
      }
      return { success: false, error: result.error };
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setIsLoading(true);
    try {
      await logoutService();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, isLoading, login, loginWithRole, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
