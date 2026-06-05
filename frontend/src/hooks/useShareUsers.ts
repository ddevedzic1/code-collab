import { useCallback, useEffect, useState } from 'react';
import { sharesApi } from '../api/sharesApi';
import type { ShareUser, ShareUserCreateRequest } from '../types/share';
import type { AppError } from '../types/api';

interface UseShareUsersResult {
  users: ShareUser[];
  loading: boolean;
  error: AppError | null;
  reload: () => void;
  addUser: (body: ShareUserCreateRequest) => Promise<void>;
  removeUser: (userId: string) => Promise<void>;
  removeAll: () => Promise<void>;
}

/** Manages the per-user permission list of a USER-type share. */
export const useShareUsers = (
  shareId: string | undefined
): UseShareUsersResult => {
  const [users, setUsers] = useState<ShareUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AppError | null>(null);

  const reload = useCallback(() => {
    if (!shareId) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    sharesApi
      .listShareUsers(shareId)
      .then(result => {
        if (active) {
          setUsers(result);
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
  }, [shareId]);

  useEffect(() => reload(), [reload]);

  const addUser = useCallback(
    async (body: ShareUserCreateRequest): Promise<void> => {
      if (!shareId) {
        throw new Error('No share id');
      }
      await sharesApi.addShareUser(shareId, body);
      reload();
    },
    [shareId, reload]
  );

  const removeUser = useCallback(
    async (userId: string): Promise<void> => {
      if (!shareId) {
        throw new Error('No share id');
      }
      await sharesApi.removeShareUser(shareId, userId);
      reload();
    },
    [shareId, reload]
  );

  const removeAll = useCallback(async (): Promise<void> => {
    if (!shareId) {
      throw new Error('No share id');
    }
    await sharesApi.removeAllShareUsers(shareId);
    reload();
  }, [shareId, reload]);

  return { users, loading, error, reload, addUser, removeUser, removeAll };
};
