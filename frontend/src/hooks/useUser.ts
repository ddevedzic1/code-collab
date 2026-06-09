import { useCallback } from 'react';
import { usersApi } from '../api/usersApi';
import { useAsyncResource } from './useAsyncResource';
import type { User, UserUpdateRequest } from '../types/user';
import type { AppError } from '../types/api';

interface UseUserResult {
  user: User | null;
  loading: boolean;
  error: AppError | null;
  refetch: () => void;
  updateUser: (body: UserUpdateRequest) => Promise<User>;
  deleteUser: () => Promise<void>;
}

export const useUser = (id: string | undefined): UseUserResult => {
  const fetcher = useCallback(() => usersApi.getUser(id as string), [id]);

  const { data, loading, error, refetch, setData } = useAsyncResource(
    id ? fetcher : null,
    [id]
  );

  const updateUser = useCallback(
    async (body: UserUpdateRequest): Promise<User> => {
      if (!id) {
        throw new Error('No user id');
      }
      const updated = await usersApi.updateUser(id, body);
      setData(updated);
      return updated;
    },
    [id, setData]
  );

  const deleteUser = useCallback(async (): Promise<void> => {
    if (!id) {
      throw new Error('No user id');
    }
    await usersApi.deleteUser(id);
  }, [id]);

  return { user: data, loading, error, refetch, updateUser, deleteUser };
};
