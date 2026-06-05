export const ExecutionStatus = {
  PENDING: 'PENDING',
  RUNNING: 'RUNNING',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
} as const;
export type ExecutionStatus =
  (typeof ExecutionStatus)[keyof typeof ExecutionStatus];

export const AuditState = {
  PENDING_AUDIT: 'PENDING_AUDIT',
  FINALIZED: 'FINALIZED',
  AUDIT_FAILED: 'AUDIT_FAILED',
} as const;
export type AuditState = (typeof AuditState)[keyof typeof AuditState];

export interface Execution {
  id: string;
  userId: string;
  snippetId: string;
  languageId: string;
  codeSnapshot: string;
  status: ExecutionStatus;
  auditState: AuditState;
  stdout: string | null;
  stderr: string | null;
  exitCode: number | null;
  durationMs: number | null;
}

export interface ExecutionCreateRequest {
  snippetId: string;
}

/** Query parameters for the execution list endpoint. `page` is zero-indexed. */
export interface ExecutionListParams {
  snippetId?: string;
  status?: ExecutionStatus;
  page?: number;
  size?: number;
  sort?: string;
}

/** True once the execution has reached a terminal state (no more polling). */
export const isTerminalStatus = (status: ExecutionStatus): boolean =>
  status === ExecutionStatus.COMPLETED || status === ExecutionStatus.FAILED;
