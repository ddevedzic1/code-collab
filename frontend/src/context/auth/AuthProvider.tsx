import { useCallback, useEffect, useRef, useState } from 'react';
import type { ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useToast } from '@chakra-ui/react';
import { AuthContext, type AuthStatus } from './AuthContext';
import { authApi } from '../../api/authApi';
import { onSessionExpired } from '../../lib/authEvents';
import { errorToast } from '../../components/toast';
import type { AuthUser, LoginRequest, RegisterRequest } from '../../types/auth';

/** Path prefixes where a 401 must NOT bounce the user to /login. */
const PUBLIC_PATH_PREFIXES = ['/login', '/register', '/s/'];

const isPublicPath = (pathname: string): boolean =>
  PUBLIC_PATH_PREFIXES.some(prefix => pathname.startsWith(prefix));

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [status, setStatus] = useState<AuthStatus>('bootstrapping');
  const [user, setUser] = useState<AuthUser | null>(null);

  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();

  const bootstrappingRef = useRef(true);
  const redirectingRef = useRef(false);
  const navigateRef = useRef(navigate);
  const locationRef = useRef(location);
  navigateRef.current = navigate;
  locationRef.current = location;

  useEffect(() => {
    let active = true;
    bootstrappingRef.current = true;

    authApi
      .validate()
      .then(validatedUser => {
        if (!active) {
          return;
        }
        setUser(validatedUser);
        setStatus('authed');
      })
      .catch(() => {
        if (!active) {
          return;
        }
        setUser(null);
        setStatus('anon');
      })
      .finally(() => {
        bootstrappingRef.current = false;
      });

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    const unsubscribe = onSessionExpired(() => {
      if (bootstrappingRef.current || redirectingRef.current) {
        return;
      }
      redirectingRef.current = true;

      setUser(null);
      setStatus('anon');

      if (!isPublicPath(locationRef.current.pathname)) {
        toast(
          errorToast({
            code: 'unauthorized',
            message: 'Your session has expired. Please sign in again.',
            isNetwork: false,
          })
        );
        navigateRef.current('/login', { replace: true });
      }

      window.setTimeout(() => {
        redirectingRef.current = false;
      }, 0);
    });

    return unsubscribe;
  }, [toast]);

  const login = useCallback(
    async (credentials: LoginRequest): Promise<AuthUser> => {
      const loggedInUser = await authApi.login(credentials);
      redirectingRef.current = false;
      setUser(loggedInUser);
      setStatus('authed');
      return loggedInUser;
    },
    []
  );

  const register = useCallback(
    async (body: RegisterRequest): Promise<void> => {
      await authApi.register(body);
      await login({ username: body.username, password: body.password });
    },
    [login]
  );

  const patchUser = useCallback((changes: Partial<AuthUser>): void => {
    setUser(current => (current ? { ...current, ...changes } : current));
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    try {
      await authApi.logout();
    } catch (error) {
      void error;
    } finally {
      setUser(null);
      setStatus('anon');
      navigateRef.current('/login', { replace: true });
    }
  }, []);

  return (
    <AuthContext.Provider
      value={{ status, user, login, register, logout, patchUser }}
    >
      {children}
    </AuthContext.Provider>
  );
};
