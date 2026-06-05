export interface User {
  id: string;
  username: string;
  email: string;
}

/** Only username and password are updatable; email is read-only. */
export interface UserUpdateRequest {
  username?: string;
  password?: string;
}
