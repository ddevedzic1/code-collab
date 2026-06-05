import { useEffect, useState } from 'react';
import { sharesApi } from '../api/sharesApi';
import type { SharedSnippet } from '../types/share';
import type { AppError } from '../types/api';

interface UseSharedSnippetResult {
  shared: SharedSnippet | null;
  loading: boolean;
  error: AppError | null;
}

/** Loads a publicly shared snippet by its share token. */
export const useSharedSnippet = (
  token: string | undefined
): UseSharedSnippetResult => {
  const [shared, setShared] = useState<SharedSnippet | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AppError | null>(null);

  useEffect(() => {
    if (!token) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    sharesApi
      .getShareByToken(token)
      .then(result => {
        if (active) {
          setShared(result);
        }
      })
      .catch((err: AppError) => {
        if (active) {
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
  }, [token]);

  return { shared, loading, error };
};
