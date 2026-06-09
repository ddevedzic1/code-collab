import { useCallback } from 'react';
import { executionsApi } from '../api/executionsApi';
import { useAsyncResource } from './useAsyncResource';
import type { PageResult } from '../types/api';
import type { Execution, ExecutionListParams } from '../types/execution';
import type { AppError } from '../types/api';

interface UseExecutionsResult {
  page: PageResult<Execution> | null;
  loading: boolean;
  error: AppError | null;
  refetch: () => void;
}

/** Lists executions (used by the editor's History tab). */
export const useExecutions = (
  params: ExecutionListParams,
  enabled = true
): UseExecutionsResult => {
  const paramsKey = JSON.stringify(params);

  const fetcher = useCallback(
    () => executionsApi.listExecutions(params),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [paramsKey]
  );

  const { data, loading, error, refetch } = useAsyncResource(
    enabled ? fetcher : null,
    [paramsKey, enabled]
  );

  return { page: data, loading, error, refetch };
};
