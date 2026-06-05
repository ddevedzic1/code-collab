/**
 * Page wrapper returned by every paginated list endpoint.
 * Field names match the backend `PageResult<T>` record exactly.
 * `currentPage` is zero-indexed.
 */
export interface PageResult<T> {
  result: T[];
  totalElements: number;
  currentPage: number;
  pageSize: number;
  totalPages: number;
}

/**
 * Error `code` values the backend returns in `{ code, message }` bodies,
 * plus client-only codes synthesized when there is no usable response body.
 */
export type ErrorCode =
  | 'internal-error'
  | 'db-error'
  | 'validation-error'
  | 'duplicate-key-error'
  | 'not-found'
  | 'forbidden'
  | 'unauthorized'
  | 'service-unavailable'
  | 'invalid-classname'
  | 'network-error'
  | 'unknown-error';

/** Raw error body shape as serialized by the backend `BaseController`. */
export interface ApiErrorBody {
  code: string;
  message: string;
}

/**
 * Normalized error produced by `normalizeError`. Every rejected request from
 * the central axios instance resolves to one of these, so hooks never deal
 * with raw `AxiosError`.
 */
export interface AppError {
  code: ErrorCode | string;
  message: string;
  status?: number;
  isNetwork: boolean;
}
