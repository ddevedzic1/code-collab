import api from './axios';
import type { AuthUser, LoginRequest, RegisterRequest } from '../types/auth';
import type { User } from '../types/user';

export const authApi = {
  register: async (body: RegisterRequest): Promise<User> => {
    const { data } = await api.post<User>('/auth/register', body);
    return data;
  },

  login: async (body: LoginRequest): Promise<AuthUser> => {
    const { data } = await api.post<AuthUser>('/auth/login', body);
    return data;
  },

  validate: async (): Promise<AuthUser> => {
    const { data } = await api.get<AuthUser>('/auth/validate');
    return data;
  },

  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  },
};
