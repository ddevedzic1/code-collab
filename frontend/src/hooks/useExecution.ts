import { useCallback, useEffect, useRef, useState } from 'react';
import { executionsApi } from '../api/executionsApi';
import { isAppError, isHandledGlobally } from '../lib/normalizeError';
import {
  isTerminalStatus,
  type Execution,
} from '../types/execution';
import type { AppError } from '../types/api';

const POLL_INTERVAL_MS = 2000;

interface UseExecutionOptions {
  /** Called when a submit or poll request fails (excluding globally-handled
   * errors like 401). The caller decides how to surface it (e.g. a toast). */
  onError?: (error: AppError) => void;
}

interface UseExecutionResult {
  execution: Execution | null;
  /** A run was submitted or a poll is scheduled / in flight. */
  isRunning: boolean;
  /** Submit a new execution for the snippet and start polling. */
  run: (snippetId: string) => Promise<void>;
  /** Load a finished or in-progress execution (resumes polling if needed). */
  loadExisting: (execution: Execution) => void;
}

/** Runs a snippet and polls its execution until it reaches a terminal state. */
export const useExecution = (
  snippetId: string | undefined,
  options: UseExecutionOptions = {}
): UseExecutionResult => {
  const [execution, setExecution] = useState<Execution | null>(null);
  const [isRunning, setIsRunning] = useState(false);

  const mountedRef = useRef(true);
  const timeoutRef = useRef<number | undefined>(undefined);
  const abortRef = useRef<AbortController | undefined>(undefined);
  const currentIdRef = useRef<string | undefined>(undefined);
  const onErrorRef = useRef(options.onError);
  onErrorRef.current = options.onError;

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
        if (isHandledGlobally(err)) {
          setIsRunning(false);
          return;
        }
        if (isAppError(err)) {
          onErrorRef.current?.(err);
        }
        setIsRunning(false);
      }
    },
    []
  );

  const run = useCallback(
    async (targetSnippetId: string) => {
      stopPolling();
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
        if (isHandledGlobally(err)) {
          setIsRunning(false);
          return;
        }
        if (isAppError(err)) {
          onErrorRef.current?.(err);
        }
        setIsRunning(false);
      }
    },
    [pollOnce, stopPolling]
  );

  const loadExisting = useCallback(
    (existing: Execution) => {
      stopPolling();
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

  useEffect(() => {
    stopPolling();
    currentIdRef.current = undefined;
    setExecution(null);
    setIsRunning(false);
  }, [snippetId, stopPolling]);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
      stopPolling();
    };
  }, [stopPolling]);

  return { execution, isRunning, run, loadExisting };
};
