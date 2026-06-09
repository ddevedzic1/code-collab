import { useCallback } from 'react';
import { sharesApi } from '../api/sharesApi';
import { useAsyncResource } from './useAsyncResource';
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
  const fetcher = useCallback(
    () => sharesApi.getShareByToken(token as string),
    [token]
  );

  // This is a public endpoint, so a 401 is meaningful and must not be hidden.
  const { data, loading, error } = useAsyncResource(
    token ? fetcher : null,
    [token],
    { suppressUnauthorized: false }
  );

  return { shared: data, loading, error };
};
