import { createContext } from 'react';
import type { AuthUser, LoginRequest, RegisterRequest } from '../../types/auth';

/** Lifecycle of the auth bootstrap. */
export type AuthStatus = 'bootstrapping' | 'authed' | 'anon';

export interface AuthContextValue {
  status: AuthStatus;
  user: AuthUser | null;
  login: (credentials: LoginRequest) => Promise<AuthUser>;
  register: (body: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  patchUser: (changes: Partial<AuthUser>) => void;
}

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined
);
