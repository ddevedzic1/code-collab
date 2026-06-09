import { useCallback } from 'react';
import { sharesApi } from '../api/sharesApi';
import { useAsyncResource } from './useAsyncResource';
import type { ShareUser, ShareUserCreateRequest } from '../types/share';
import type { AppError } from '../types/api';

interface UseShareUsersResult {
  users: ShareUser[];
  loading: boolean;
  error: AppError | null;
  reload: () => void;
  addUser: (body: ShareUserCreateRequest) => Promise<void>;
  removeUser: (username: string) => Promise<void>;
  removeAll: () => Promise<void>;
}

/** Manages the per-user permission list of a USER-type share. */
export const useShareUsers = (
  shareId: string | undefined
): UseShareUsersResult => {
  const fetcher = useCallback(
    () => sharesApi.listShareUsers(shareId as string),
    [shareId]
  );

  const { data, loading, error, refetch } = useAsyncResource(
    shareId ? fetcher : null,
    [shareId]
  );

  const addUser = useCallback(
    async (body: ShareUserCreateRequest): Promise<void> => {
      if (!shareId) {
        throw new Error('No share id');
      }
      await sharesApi.addShareUser(shareId, body);
      refetch();
    },
    [shareId, refetch]
  );

  const removeUser = useCallback(
    async (username: string): Promise<void> => {
      if (!shareId) {
        throw new Error('No share id');
      }
      await sharesApi.removeShareUser(shareId, username);
      refetch();
    },
    [shareId, refetch]
  );

  const removeAll = useCallback(async (): Promise<void> => {
    if (!shareId) {
      throw new Error('No share id');
    }
    await sharesApi.removeAllShareUsers(shareId);
    refetch();
  }, [shareId, refetch]);

  return {
    users: data ?? [],
    loading,
    error,
    reload: refetch,
    addUser,
    removeUser,
    removeAll,
  };
};
