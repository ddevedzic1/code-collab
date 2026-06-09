import { useCallback, useEffect, useState } from 'react';
import type { AppError } from '../types/api';

interface UseAsyncResourceOptions {
  /**
   * Suppress 401 errors (the global auth handler already redirects on those).
   * Defaults to true; set false on public endpoints where a 401 is meaningful.
   */
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
 * Generic data-fetching hook shared by the resource hooks. It owns the
 * recurring boilerplate: the `active` guard against stale/unmounted updates,
 * loading/error state, and 401 suppression.
 *
 * Pass `fetcher: null` to disable the fetch (e.g. a missing id or a deferred
 * `enabled` flag); the hook then stays idle with `loading === false`.
 *
 * `deps` controls when a refetch fires — exactly like a `useEffect` dependency
 * array. The fetcher is intentionally not part of the dependency contract, so
 * callers pass a value-based key (id, serialized params) instead of worrying
 * about the fetcher's identity.
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
        if (active && !(suppressUnauthorized && err.status === 401)) {
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
    // The fetcher's identity is deliberately excluded; callers drive refetches
    // through `deps` (id / serialized params / enabled flag).
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => load(), [load]);

  return { data, loading, error, refetch: load, setData };
};
