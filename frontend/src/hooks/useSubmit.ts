import { useCallback, useState } from 'react';
import { useToast } from '@chakra-ui/react';
import { errorToast, successToast } from '../components/toast';
import { isAppError } from '../lib/normalizeError';

interface RunOptions<T> {
  /** Run before submitting; return false to abort (e.g. failed validation). */
  guard?: () => boolean;
  /** Toast shown on success. */
  successMessage?: string;
  /** Called with the action's result on success (after the success toast). */
  onSuccess?: (result: T) => void | Promise<void>;
  /** Called with the normalized error on failure (after the error toast). */
  onError?: (error: unknown) => void;
  /**
   * Keep `submitting` true after a successful run. Use when success navigates
   * away or unmounts the component, so the control never re-enables.
   */
  keepSubmittingOnSuccess?: boolean;
}

interface UseSubmitResult {
  submitting: boolean;
  /** Run an async action with loading state and success/error toasts. */
  run: <T>(action: () => Promise<T>, options?: RunOptions<T>) => Promise<void>;
}

/**
 * Wraps the recurring form-submit pattern: a `submitting` flag, a built-in
 * double-submit guard, success/error toasts, and reset of the flag afterwards.
 */
export const useSubmit = (): UseSubmitResult => {
  const toast = useToast();
  const [submitting, setSubmitting] = useState(false);

  const run = useCallback(
    async <T>(
      action: () => Promise<T>,
      options: RunOptions<T> = {}
    ): Promise<void> => {
      const {
        guard,
        successMessage,
        onSuccess,
        onError,
        keepSubmittingOnSuccess = false,
      } = options;

      if (submitting || (guard && !guard())) {
        return;
      }

      setSubmitting(true);
      try {
        const result = await action();
        if (successMessage) {
          toast(successToast(successMessage));
        }
        await onSuccess?.(result);
        if (!keepSubmittingOnSuccess) {
          setSubmitting(false);
        }
      } catch (error) {
        if (isAppError(error)) {
          toast(errorToast(error));
        }
        onError?.(error);
        setSubmitting(false);
      }
    },
    [submitting, toast]
  );

  return { submitting, run };
};
