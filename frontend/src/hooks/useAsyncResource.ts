import { useCallback, useEffect, useState } from 'react';
import { isHandledGlobally } from '../lib/normalizeError';
import type { AppError } from '../types/api';

interface UseAsyncResourceOptions {
  /** Suppress errors handled by the global auth flow. Defaults to true. */
  suppressUnauthorized?: boolean;
}

export interface AsyncResource<T> {
  data: T | null;
  loading: boolean;
  error: AppError | null;
  refetch: () => void;
  /** Imperatively replace the cached data (e.g. after a mutation response). */
  setData: (data: T | null) => void;
}

/**
 * Generic data-fetching hook: owns loading/error state, the active guard, and
 * 401 suppression. Pass `fetcher: null` to disable; `deps` drives refetches.
 */
export const useAsyncResource = <T>(
  fetcher: (() => Promise<T>) | null,
  deps: ReadonlyArray<unknown>,
  options: UseAsyncResourceOptions = {}
): AsyncResource<T> => {
  const { suppressUnauthorized = true } = options;
  const enabled = fetcher !== null;

  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState<AppError | null>(null);

  const load = useCallback(() => {
    if (!fetcher) {
      return;
    }
    let active = true;
    setLoading(true);
    setError(null);

    fetcher()
      .then(result => {
        if (active) {
          setData(result);
        }
      })
      .catch((err: AppError) => {
        if (active && !(suppressUnauthorized && isHandledGlobally(err))) {
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
  }, deps);

  useEffect(() => load(), [load]);

  return { data, loading, error, refetch: load, setData };
};
