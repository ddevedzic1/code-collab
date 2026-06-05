import { useCallback, useEffect, useState } from 'react';
import { snippetsApi } from '../api/snippetsApi';
import type { PageResult } from '../types/api';
import type { Snippet, SnippetListParams } from '../types/snippet';
import type { AppError } from '../types/api';

interface UseSnippetsResult {
  page: PageResult<Snippet> | null;
  loading: boolean;
  error: AppError | null;
  refetch: () => void;
}

/**
 * Fetches a page of the current user's snippets. Re-fetches whenever the
 * serialized params change (title / languageId / page / size / sort).
 */
export const useSnippets = (params: SnippetListParams): UseSnippetsResult => {
  const [page, setPage] = useState<PageResult<Snippet> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<AppError | null>(null);

  const paramsKey = JSON.stringify(params);

  const fetchSnippets = useCallback(() => {
    let active = true;
    setLoading(true);
    setError(null);

    snippetsApi
      .listSnippets(params)
      .then(result => {
        if (active) {
          setPage(result);
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
    // params is captured via paramsKey to re-run on value (not reference) change
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [paramsKey]);

  useEffect(() => fetchSnippets(), [fetchSnippets]);

  return { page, loading, error, refetch: fetchSnippets };
};
