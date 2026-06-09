import { useCallback, useEffect, useState } from 'react';
import { sharesApi } from '../api/sharesApi';
import { isHandledGlobally } from '../lib/normalizeError';
import type {
  Share,
  ShareCreateRequest,
  ShareUpdateRequest,
} from '../types/share';
import type { AppError } from '../types/api';

export type ShareStatus = 'idle' | 'loading' | 'none' | 'has' | 'error';

interface UseShareResult {
  status: ShareStatus;
  share: Share | null;
  error: AppError | null;
  reload: () => void;
  createShare: (body: ShareCreateRequest) => Promise<Share>;
  updateShare: (body: ShareUpdateRequest) => Promise<Share>;
  deleteShare: () => Promise<void>;
}

/** Loads and mutates the single active share for a snippet. */
export const useShare = (
  snippetId: string | undefined,
  enabled: boolean
): UseShareResult => {
  const [status, setStatus] = useState<ShareStatus>('idle');
  const [share, setShare] = useState<Share | null>(null);
  const [error, setError] = useState<AppError | null>(null);

  const reload = useCallback(() => {
    if (!snippetId || !enabled) {
      return;
    }
    let active = true;
    setStatus('loading');
    setError(null);

    sharesApi
      .getShare(snippetId)
      .then(result => {
        if (!active) {
          return;
        }
        setShare(result);
        setStatus(result ? 'has' : 'none');
      })
      .catch((err: AppError) => {
        if (!active) {
          return;
        }
        if (isHandledGlobally(err)) {
          return;
        }
        setError(err);
        setStatus('error');
      });

    return () => {
      active = false;
    };
  }, [snippetId, enabled]);

  useEffect(() => reload(), [reload]);

  const createShare = useCallback(
    async (body: ShareCreateRequest): Promise<Share> => {
      if (!snippetId) {
        throw new Error('No snippet id');
      }
      const created = await sharesApi.createShare(snippetId, body);
      setShare(created);
      setStatus('has');
      return created;
    },
    [snippetId]
  );

  const updateShare = useCallback(
    async (body: ShareUpdateRequest): Promise<Share> => {
      if (!share) {
        throw new Error('No share to update');
      }
      const updated = await sharesApi.updateShare(share.id, body);
      setShare(updated);
      setStatus('has');
      return updated;
    },
    [share]
  );

  const deleteShare = useCallback(async (): Promise<void> => {
    if (!share) {
      throw new Error('No share to delete');
    }
    await sharesApi.deleteShare(share.id);
    setShare(null);
    setStatus('none');
  }, [share]);

  return {
    status,
    share,
    error,
    reload,
    createShare,
    updateShare,
    deleteShare,
  };
};
