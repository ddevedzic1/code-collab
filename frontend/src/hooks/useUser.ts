import { useCallback, useEffect, useState } from 'react';
import { usersApi } from '../api/usersApi';
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
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AppError | null>(null);

  const fetchUser = useCallback(() => {
    if (!id) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    usersApi
      .getUser(id)
      .then(result => {
        if (active) {
          setUser(result);
        }
      })
      .catch((err: AppError) => {
        if (active && err.status !== 401) {
          setError(err);
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => fetchUser(), [fetchUser]);

  const updateUser = useCallback(
    async (body: UserUpdateRequest): Promise<User> => {
      if (!id) {
        throw new Error('No user id');
      }
      const updated = await usersApi.updateUser(id, body);
      setUser(updated);
      return updated;
    },
    [id]
  );

  const deleteUser = useCallback(async (): Promise<void> => {
    if (!id) {
      throw new Error('No user id');
    }
    await usersApi.deleteUser(id);
  }, [id]);

  return { user, loading, error, refetch: fetchUser, updateUser, deleteUser };
};
