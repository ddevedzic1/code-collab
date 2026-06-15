import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { useExecution } from './useExecution';
import { executionsApi } from '../api/executionsApi';
import { ExecutionStatus, AuditState, type Execution } from '../types/execution';
import type { AppError } from '../types/api';

vi.mock('../api/executionsApi', () => ({
  executionsApi: {
    createExecution: vi.fn(),
    getExecution: vi.fn(),
  },
}));

const createExecution = vi.mocked(executionsApi.createExecution);
const getExecution = vi.mocked(executionsApi.getExecution);

const makeExecution = (overrides: Partial<Execution> = {}): Execution => ({
  id: 'exec-1',
  userId: 'u1',
  snippetId: 's1',
  languageId: 'l1',
  codeSnapshot: 'print(1)',
  status: ExecutionStatus.PENDING,
  auditState: AuditState.PENDING_AUDIT,
  stdout: null,
  stderr: null,
  exitCode: null,
  durationMs: null,
  ...overrides,
});

beforeEach(() => {
  vi.useFakeTimers();
  createExecution.mockReset();
  getExecution.mockReset();
});

afterEach(() => {
  vi.runOnlyPendingTimers();
  vi.useRealTimers();
});

describe('useExecution', () => {
  it('submits a run, polls until a terminal status, then stops', async () => {
    createExecution.mockResolvedValue(makeExecution({ status: ExecutionStatus.PENDING }));
    getExecution
      .mockResolvedValueOnce(makeExecution({ status: ExecutionStatus.RUNNING }))
      .mockResolvedValueOnce(
        makeExecution({
          status: ExecutionStatus.COMPLETED,
          stdout: '1\n',
          exitCode: 0,
          durationMs: 42,
        })
      );

    const { result } = renderHook(() => useExecution('s1'));

    await act(async () => {
      await result.current.run('s1');
    });

    expect(createExecution).toHaveBeenCalledWith({ snippetId: 's1' });
    expect(result.current.execution?.status).toBe(ExecutionStatus.PENDING);
    expect(result.current.isRunning).toBe(true);

    // First poll → RUNNING (still polling).
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });
    expect(result.current.execution?.status).toBe(ExecutionStatus.RUNNING);
    expect(result.current.isRunning).toBe(true);

    // Second poll → COMPLETED (stops).
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });
    expect(result.current.execution?.status).toBe(ExecutionStatus.COMPLETED);
    expect(result.current.execution?.exitCode).toBe(0);
    expect(result.current.isRunning).toBe(false);

    // No further polling once terminal.
    const callsAfterTerminal = getExecution.mock.calls.length;
    await act(async () => {
      await vi.advanceTimersByTimeAsync(4000);
    });
    expect(getExecution.mock.calls.length).toBe(callsAfterTerminal);
  });

  it('reports a submit failure via onError without entering a polling loop', async () => {
    const error: AppError = {
      code: 'service-unavailable',
      message: 'unavailable',
      status: 503,
      isNetwork: false,
    };
    createExecution.mockRejectedValue(error);
    const onError = vi.fn();

    const { result } = renderHook(() => useExecution('s1', { onError }));

    await act(async () => {
      await result.current.run('s1');
    });

    expect(onError).toHaveBeenCalledWith(error);
    expect(result.current.isRunning).toBe(false);
    expect(getExecution).not.toHaveBeenCalled();
  });

  it('does not surface a 401 (handled globally) via onError', async () => {
    createExecution.mockRejectedValue({
      code: 'unauthorized',
      message: 'no session',
      status: 401,
      isNetwork: false,
    } satisfies AppError);
    const onError = vi.fn();

    const { result } = renderHook(() => useExecution('s1', { onError }));

    await act(async () => {
      await result.current.run('s1');
    });

    expect(onError).not.toHaveBeenCalled();
    expect(result.current.isRunning).toBe(false);
  });

  it('loadExisting shows a terminal execution without polling', async () => {
    const { result } = renderHook(() => useExecution('s1'));

    act(() => {
      result.current.loadExisting(
        makeExecution({ id: 'old', status: ExecutionStatus.FAILED })
      );
    });

    expect(result.current.execution?.id).toBe('old');
    expect(result.current.isRunning).toBe(false);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(4000);
    });
    expect(getExecution).not.toHaveBeenCalled();
  });

  it('loadExisting resumes polling for an in-progress execution', async () => {
    getExecution.mockResolvedValue(
      makeExecution({ id: 'running-1', status: ExecutionStatus.COMPLETED })
    );

    const { result } = renderHook(() => useExecution('s1'));

    act(() => {
      result.current.loadExisting(
        makeExecution({ id: 'running-1', status: ExecutionStatus.RUNNING })
      );
    });
    expect(result.current.isRunning).toBe(true);

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    expect(getExecution).toHaveBeenCalledWith('running-1', expect.anything());
    expect(result.current.execution?.status).toBe(ExecutionStatus.COMPLETED);
    expect(result.current.isRunning).toBe(false);
  });

  it('stops polling after unmount', async () => {
    createExecution.mockResolvedValue(makeExecution({ status: ExecutionStatus.PENDING }));
    getExecution.mockResolvedValue(makeExecution({ status: ExecutionStatus.RUNNING }));

    const { result, unmount } = renderHook(() => useExecution('s1'));

    await act(async () => {
      await result.current.run('s1');
    });

    unmount();
    const callsAtUnmount = getExecution.mock.calls.length;

    await act(async () => {
      await vi.advanceTimersByTimeAsync(6000);
    });
    expect(getExecution.mock.calls.length).toBe(callsAtUnmount);
  });
});
