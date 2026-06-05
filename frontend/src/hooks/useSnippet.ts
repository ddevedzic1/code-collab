import { useCallback, useEffect, useState } from 'react';
import { snippetsApi } from '../api/snippetsApi';
import type { Snippet } from '../types/snippet';
import type { AppError } from '../types/api';

interface UseSnippetResult {
  snippet: Snippet | null;
  loading: boolean;
  error: AppError | null;
  refetch: () => void;
  /** Replace the local snippet (e.g. after a PATCH returns the updated DTO). */
  setSnippet: (snippet: Snippet) => void;
}

export const useSnippet = (id: string | undefined): UseSnippetResult => {
  const [snippet, setSnippet] = useState<Snippet | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AppError | null>(null);

  const fetchSnippet = useCallback(() => {
    if (!id) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    snippetsApi
      .getSnippet(id)
      .then(result => {
        if (active) {
          setSnippet(result);
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

  useEffect(() => fetchSnippet(), [fetchSnippet]);

  return { snippet, loading, error, refetch: fetchSnippet, setSnippet };
};
