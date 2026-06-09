import { useCallback } from 'react';
import { snippetsApi } from '../api/snippetsApi';
import { useAsyncResource } from './useAsyncResource';
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
  const paramsKey = JSON.stringify(params);

  const fetcher = useCallback(
    () => snippetsApi.listSnippets(params),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [paramsKey]
  );

  const { data, loading, error, refetch } = useAsyncResource(fetcher, [
    paramsKey,
  ]);

  return { page: data, loading, error, refetch };
};
