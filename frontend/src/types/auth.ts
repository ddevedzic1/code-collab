/** Identity of the authenticated user, as returned by login and validate. */
export interface AuthUser {
  id: string;
  username: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}
