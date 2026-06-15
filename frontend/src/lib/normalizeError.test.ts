import { describe, it, expect } from 'vitest';
import { AxiosError, AxiosHeaders } from 'axios';
import { normalizeError, isAppError, isHandledGlobally } from './normalizeError';

const axiosErrorWithResponse = (status: number, data: unknown): AxiosError => {
  const error = new AxiosError('Request failed');
  error.response = {
    status,
    data,
    statusText: '',
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
  return error;
};

const axiosErrorWithRequest = (): AxiosError => {
  const error = new AxiosError('Network Error');
  // A request was made but no response received (server unreachable).
  error.request = {};
  return error;
};

describe('normalizeError', () => {
  it('uses the backend {code,message} body when present', () => {
    const result = normalizeError(
      axiosErrorWithResponse(404, {
        code: 'not-found',
        message: 'Snippet with id 1 was not found',
      })
    );
    expect(result).toEqual({
      code: 'not-found',
      message: 'Snippet with id 1 was not found',
      status: 404,
      isNetwork: false,
    });
  });

  it('synthesizes a friendly message from status when the body is not the error shape', () => {
    const result = normalizeError(axiosErrorWithResponse(503, '<html>502 Bad Gateway</html>'));
    expect(result.code).toBe('service-unavailable');
    expect(result.status).toBe(503);
    expect(result.isNetwork).toBe(false);
    expect(result.message).toMatch(/temporarily unavailable/i);
  });

  it('falls back to unknown-error for an unmapped status with no usable body', () => {
    const result = normalizeError(axiosErrorWithResponse(418, null));
    expect(result.code).toBe('unknown-error');
    expect(result.status).toBe(418);
  });

  it('reports a network error when a request was made but no response received', () => {
    const result = normalizeError(axiosErrorWithRequest());
    expect(result.code).toBe('network-error');
    expect(result.isNetwork).toBe(true);
    expect(result.status).toBeUndefined();
  });

  it('handles a plain Error', () => {
    const result = normalizeError(new Error('boom'));
    expect(result).toEqual({
      code: 'unknown-error',
      message: 'boom',
      isNetwork: false,
    });
  });

  it('handles a non-error thrown value', () => {
    const result = normalizeError('just a string');
    expect(result.code).toBe('unknown-error');
    expect(result.isNetwork).toBe(false);
  });
});

describe('isAppError', () => {
  it('accepts a normalized error and rejects arbitrary objects', () => {
    expect(isAppError(normalizeError(new Error('x')))).toBe(true);
    expect(isAppError({ code: 'x', message: 'y' })).toBe(false); // missing isNetwork
    expect(isAppError(null)).toBe(false);
    expect(isAppError('nope')).toBe(false);
  });
});

describe('isHandledGlobally', () => {
  it('is true only for a 401 AppError', () => {
    expect(isHandledGlobally(normalizeError(axiosErrorWithResponse(401, null)))).toBe(true);
    expect(isHandledGlobally(normalizeError(axiosErrorWithResponse(403, null)))).toBe(false);
    expect(isHandledGlobally(new Error('x'))).toBe(false);
  });
});
