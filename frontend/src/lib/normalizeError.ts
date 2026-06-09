import axios from 'axios';
import type { AppError, ApiErrorBody, ErrorCode } from '../types/api';

const STATUS_CODE_MAP: Record<number, ErrorCode> = {
  400: 'validation-error',
  401: 'unauthorized',
  403: 'forbidden',
  404: 'not-found',
  500: 'internal-error',
  503: 'service-unavailable',
};

const STATUS_MESSAGE_MAP: Record<number, string> = {
  400: 'The request was invalid. Please check your input and try again.',
  401: 'Your session is no longer valid. Please sign in again.',
  403: 'You do not have permission to perform this action.',
  404: 'The requested resource was not found.',
  500: 'Something went wrong. Please try again later.',
  503: 'The system is temporarily unavailable. Please try again later.',
};

const isApiErrorBody = (data: unknown): data is ApiErrorBody => {
  return (
    typeof data === 'object' &&
    data !== null &&
    typeof (data as Record<string, unknown>).code === 'string' &&
    typeof (data as Record<string, unknown>).message === 'string'
  );
};

const fromStatus = (status: number): AppError => ({
  code: STATUS_CODE_MAP[status] ?? 'unknown-error',
  message:
    STATUS_MESSAGE_MAP[status] ?? 'Something went wrong. Please try again.',
  status,
  isNetwork: false,
});

/**
 * Convert any thrown value into a stable {@link AppError}. The central axios
 * interceptor runs this on every rejection, so downstream `catch` blocks
 * always receive an AppError rather than a raw AxiosError.
 */
export const normalizeError = (error: unknown): AppError => {
  if (axios.isAxiosError(error)) {
    if (error.response) {
      const { status, data } = error.response;
      if (isApiErrorBody(data)) {
        return { code: data.code, message: data.message, status, isNetwork: false };
      }
      return fromStatus(status);
    }

    if (error.request) {
      return {
        code: 'network-error',
        message: 'Cannot reach the server. Check your connection and try again.',
        isNetwork: true,
      };
    }

    return { code: 'unknown-error', message: error.message, isNetwork: false };
  }

  if (error instanceof Error) {
    return { code: 'unknown-error', message: error.message, isNetwork: false };
  }

  return {
    code: 'unknown-error',
    message: 'An unexpected error occurred.',
    isNetwork: false,
  };
};

/** Type guard so components can narrow an unknown catch value to AppError. */
export const isAppError = (value: unknown): value is AppError => {
  return (
    typeof value === 'object' &&
    value !== null &&
    typeof (value as Record<string, unknown>).code === 'string' &&
    typeof (value as Record<string, unknown>).message === 'string' &&
    typeof (value as Record<string, unknown>).isNetwork === 'boolean'
  );
};

/**
 * True when an error is already handled by the global auth flow and should not
 * be surfaced locally. A 401 triggers the axios interceptor's session-expired
 * event, which clears auth and redirects; re-displaying it in a hook would just
 * produce a duplicate, soon-to-be-unmounted error. Hooks suppress these.
 */
export const isHandledGlobally = (error: unknown): boolean => {
  return isAppError(error) && error.status === 401;
};
