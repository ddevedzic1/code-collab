import { useCallback, useEffect, useRef, useState } from 'react';
import { executionsApi } from '../api/executionsApi';
import { isAppError } from '../lib/normalizeError';
import {
  isTerminalStatus,
  type Execution,
} from '../types/execution';
import type { AppError } from '../types/api';

const POLL_INTERVAL_MS = 2000;

interface UseExecutionResult {
  execution: Execution | null;
  /** A run was submitted or a poll is scheduled / in flight. */
  isRunning: boolean;
  error: AppError | null;
  /** Submit a new execution for the snippet and start polling. */
  run: (snippetId: string) => Promise<void>;
  /** Load a finished or in-progress execution (resumes polling if needed). */
  loadExisting: (execution: Execution) => void;
}

/**
 * Manages running a snippet and polling its execution until a terminal state.
 *
 * Polling uses a recursive setTimeout (never setInterval) so requests never
 * overlap. Several refs guard against the classic async hazards:
 *  - mountedRef:    stop all state updates after unmount
 *  - timeoutRef:    only ever one scheduled poll
 *  - abortRef:      cancel the in-flight GET on teardown / replacement
 *  - currentIdRef:  drop stale responses when the tracked execution changes
 */
export const useExecution = (snippetId: string | undefined): UseExecutionResult => {
  const [execution, setExecution] = useState<Execution | null>(null);
  const [isRunning, setIsRunning] = useState(false);
  const [error, setError] = useState<AppError | null>(null);

  const mountedRef = useRef(true);
  const timeoutRef = useRef<number | undefined>(undefined);
  const abortRef = useRef<AbortController | undefined>(undefined);
  const currentIdRef = useRef<string | undefined>(undefined);

  const stopPolling = useCallback(() => {
    if (timeoutRef.current !== undefined) {
      window.clearTimeout(timeoutRef.current);
      timeoutRef.current = undefined;
    }
    abortRef.current?.abort();
    abortRef.current = undefined;
  }, []);

  const pollOnce = useCallback(
    async (id: string) => {
      if (!mountedRef.current || id !== currentIdRef.current) {
        return;
      }

      const controller = new AbortController();
      abortRef.current = controller;

      try {
        const result = await executionsApi.getExecution(id, {
          signal: controller.signal,
        });

        if (!mountedRef.current || id !== currentIdRef.current) {
          return;
        }

        setExecution(result);

        if (isTerminalStatus(result.status)) {
          setIsRunning(false);
          return;
        }

        timeoutRef.current = window.setTimeout(
          () => pollOnce(id),
          POLL_INTERVAL_MS
        );
      } catch (err) {
        if (controller.signal.aborted) {
          return;
        }
        if (!mountedRef.current || id !== currentIdRef.current) {
          return;
        }
        // 401 is handled globally (auth context redirects); don't surface it.
        if (isAppError(err) && err.status === 401) {
          setIsRunning(false);
          return;
        }
        if (isAppError(err)) {
          setError(err);
        }
        setIsRunning(false);
      }
    },
    []
  );

  const run = useCallback(
    async (targetSnippetId: string) => {
      stopPolling();
      setError(null);
      setIsRunning(true);

      try {
        const created = await executionsApi.createExecution({
          snippetId: targetSnippetId,
        });
        if (!mountedRef.current) {
          return;
        }
        currentIdRef.current = created.id;
        setExecution(created);

        if (isTerminalStatus(created.status)) {
          setIsRunning(false);
          return;
        }
        timeoutRef.current = window.setTimeout(
          () => pollOnce(created.id),
          POLL_INTERVAL_MS
        );
      } catch (err) {
        if (!mountedRef.current) {
          return;
        }
        if (isAppError(err) && err.status === 401) {
          setIsRunning(false);
          return;
        }
        if (isAppError(err)) {
          setError(err);
        }
        setIsRunning(false);
      }
    },
    [pollOnce, stopPolling]
  );

  const loadExisting = useCallback(
    (existing: Execution) => {
      stopPolling();
      setError(null);
      currentIdRef.current = existing.id;
      setExecution(existing);

      if (isTerminalStatus(existing.status)) {
        setIsRunning(false);
        return;
      }
      setIsRunning(true);
      timeoutRef.current = window.setTimeout(
        () => pollOnce(existing.id),
        POLL_INTERVAL_MS
      );
    },
    [pollOnce, stopPolling]
  );

  // Reset everything when the editor switches to a different snippet.
  useEffect(() => {
    stopPolling();
    currentIdRef.current = undefined;
    setExecution(null);
    setIsRunning(false);
    setError(null);
  }, [snippetId, stopPolling]);

  // Track mount state and tear down on unmount.
  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      stopPolling();
    };
  }, [stopPolling]);

  return { execution, isRunning, error, run, loadExisting };
};
