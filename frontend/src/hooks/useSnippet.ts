import { useCallback } from 'react';
import { snippetsApi } from '../api/snippetsApi';
import { useAsyncResource } from './useAsyncResource';
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
  const fetcher = useCallback(
    () => snippetsApi.getSnippet(id as string),
    [id]
  );

  const { data, loading, error, refetch, setData } = useAsyncResource(
    id ? fetcher : null,
    [id]
  );

  return {
    snippet: data,
    loading,
    error,
    refetch,
    setSnippet: setData,
  };
};
