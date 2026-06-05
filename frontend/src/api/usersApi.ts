import api from './axios';
import type { User, UserUpdateRequest } from '../types/user';

export const usersApi = {
  getUser: async (id: string): Promise<User> => {
    const { data } = await api.get<User>(`/users/${id}`);
    return data;
  },

  updateUser: async (id: string, body: UserUpdateRequest): Promise<User> => {
    const { data } = await api.patch<User>(`/users/${id}`, body);
    return data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/users/${id}`);
  },
};
