import { useCallback, useEffect, useState } from 'react';
import { executionsApi } from '../api/executionsApi';
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
  const [page, setPage] = useState<PageResult<Execution> | null>(null);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState<AppError | null>(null);

  const paramsKey = JSON.stringify(params);

  const fetchExecutions = useCallback(() => {
    if (!enabled) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    executionsApi
      .listExecutions(params)
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [paramsKey, enabled]);

  useEffect(() => fetchExecutions(), [fetchExecutions]);

  return { page, loading, error, refetch: fetchExecutions };
};
