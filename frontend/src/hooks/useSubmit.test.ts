import { describe, it, expect, vi, beforeEach } from 'vitest';
import { act, renderHook, waitFor } from '@testing-library/react';
import { useSubmit } from './useSubmit';
import type { AppError } from '../types/api';

const toastSpy = vi.fn();

vi.mock('@chakra-ui/react', () => ({
  useToast: () => toastSpy,
}));

beforeEach(() => {
  toastSpy.mockClear();
});

const appError: AppError = {
  code: 'service-unavailable',
  message: 'The system is temporarily unavailable. Please try again later.',
  status: 503,
  isNetwork: false,
};

describe('useSubmit', () => {
  it('toggles submitting around a successful action and toasts success', async () => {
    const { result } = renderHook(() => useSubmit());
    const action = vi.fn().mockResolvedValue({ id: '1' });
    const onSuccess = vi.fn();

    await act(async () => {
      await result.current.run(action, {
        successMessage: 'Saved.',
        onSuccess,
      });
    });

    expect(action).toHaveBeenCalledOnce();
    expect(onSuccess).toHaveBeenCalledWith({ id: '1' });
    expect(toastSpy).toHaveBeenCalledOnce();
    expect(result.current.submitting).toBe(false);
  });

  it('aborts when the guard returns false', async () => {
    const { result } = renderHook(() => useSubmit());
    const action = vi.fn().mockResolvedValue(undefined);

    await act(async () => {
      await result.current.run(action, { guard: () => false });
    });

    expect(action).not.toHaveBeenCalled();
    expect(result.current.submitting).toBe(false);
  });

  it('toasts the AppError and runs onError on failure', async () => {
    const { result } = renderHook(() => useSubmit());
    const action = vi.fn().mockRejectedValue(appError);
    const onError = vi.fn();

    await act(async () => {
      await result.current.run(action, { onError });
    });

    expect(toastSpy).toHaveBeenCalledOnce();
    expect(onError).toHaveBeenCalledWith(appError);
    expect(result.current.submitting).toBe(false);
  });

  it('prevents a concurrent double-submit', async () => {
    const { result } = renderHook(() => useSubmit());

    let resolveFirst: (value: unknown) => void = () => {};
    const slowAction = vi.fn(
      () => new Promise(resolve => {
        resolveFirst = resolve;
      })
    );
    const secondAction = vi.fn().mockResolvedValue(undefined);

    // Start the first (still pending) run.
    let firstRun: Promise<void> = Promise.resolve();
    act(() => {
      firstRun = result.current.run(slowAction);
    });
    await waitFor(() => expect(result.current.submitting).toBe(true));

    // A second run while the first is in flight must be ignored.
    await act(async () => {
      await result.current.run(secondAction);
    });
    expect(secondAction).not.toHaveBeenCalled();

    await act(async () => {
      resolveFirst(undefined);
      await firstRun;
    });
    expect(slowAction).toHaveBeenCalledOnce();
    expect(result.current.submitting).toBe(false);
  });

  it('keeps submitting true after success when keepSubmittingOnSuccess is set', async () => {
    const { result } = renderHook(() => useSubmit());

    await act(async () => {
      await result.current.run(() => Promise.resolve(), {
        keepSubmittingOnSuccess: true,
      });
    });

    expect(result.current.submitting).toBe(true);
  });
});
