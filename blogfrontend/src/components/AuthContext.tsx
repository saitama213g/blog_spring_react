import React, { createContext, useContext, useState, useCallback, useEffect, useRef } from 'react';
import { apiService } from '../services/apiService';

interface AuthUser {
  id: string;
  name: string;
  email: string;
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: AuthUser | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  token: string | null;
}

interface AuthProviderProps {
  children: React.ReactNode;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>("");
  const [tokenExp, setTokenExp] = useState<number>(0);
  const refreshTimeoutRef = useRef<number | null>(null);

  // Initialize auth state from token
  useEffect(() => {
    const initializeAuth = async () => {
      // const storedToken = localStorage.getItem('token');
      // const storedExpireAt = Number(localStorage.getItem('expiresAt')) || 0 ;
      // if (storedToken && storedExpireAt) {
        try {
          // TODO: Add endpoint to fetch user profile
          // const userProfile = await apiService.getUserProfile();
          // setUser(userProfile);
          setIsAuthenticated(true);
          // setToken(storedToken);
          // setTokenExp(storedExpireAt)
        } catch (error) {
          // If token is invalid, clear authentication
          localStorage.removeItem('token');
          setIsAuthenticated(false);
          setUser(null);
          setToken(null);
        }
      // }
    };

    initializeAuth();
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    try {
      const response = await apiService.login({ email, password });

      // Only set state if response is valid
      if (response && response.token) {
        setToken(response.token);
        setTokenExp(response.expiresAt);
        setIsAuthenticated(true);

        // Optional: fetch user profile after login
        // const userProfile = await apiService.getUserProfile();
        // setUser(userProfile);
      } else {
        throw new Error('Login failed: invalid response');
      }
    } catch (error) {
      // Don't set anything on error
      console.error('Login error:', error);
      throw error;
    }
  }, []);
  
  const register = useCallback(async (name: string, email: string, password: string) => {
    try {
      const response = await apiService.register({ name, email, password });
      
      localStorage.setItem('token', response.token);
      setToken(response.token);
      setTokenExp(response.expiresAt);
      setIsAuthenticated(true);

      // TODO: Add endpoint to fetch user profile after registration
      // const userProfile = await apiService.getUserProfile();
      // setUser(userProfile);
    } catch (error) {
      throw error;
    }
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
    setUser(null);
    setToken(null);
    setTokenExp(0);
    apiService.logout(); // This clears the token from apiService
  }, []);

  const refreshAccessToken = useCallback(async () => {
    try {
      const response = await apiService.refreshToken();
      setToken(response.token);
      setTokenExp(response.expiresAt);
      console.log("token got refreshed");
    } catch (err) {
      logout();
    }
  }, [logout]);


  useEffect(() => {
    if (!tokenExp) return;

    const REFRESH_OFFSET = 60_000; // 1 min before expiry
    const now = Date.now();
    const delay = tokenExp * 1000 - now - REFRESH_OFFSET;

    // If already expired, refresh immediately
    if (delay <= 0) {
      refreshAccessToken();
      return;
    }

    // Clear previous timeout if exists
    if (refreshTimeoutRef.current) {
      clearTimeout(refreshTimeoutRef.current);
    }

    // Schedule new refresh
    refreshTimeoutRef.current = setTimeout(() => {
      refreshAccessToken();
      refreshTimeoutRef.current = null;
    }, delay);

    return () => {
      if (refreshTimeoutRef.current) clearTimeout(refreshTimeoutRef.current);
    };
  }, [tokenExp, refreshAccessToken]);

  // Update apiService token when it changes
  useEffect(() => {
    if (token) {
      // Update axios instance configuration
      const axiosInstance = apiService['api'];
      axiosInstance.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    }
  }, [token]);

  const value = {
    isAuthenticated,
    user,
    login,
    register,
    logout,
    token,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;