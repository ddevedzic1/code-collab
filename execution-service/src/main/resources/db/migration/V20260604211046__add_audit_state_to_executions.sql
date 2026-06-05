ALTER TABLE executions
    ADD COLUMN IF NOT EXISTS audit_state VARCHAR(30) NOT NULL DEFAULT 'FINALIZED';

ALTER TABLE executions
    DROP CONSTRAINT IF EXISTS chk_executions_audit_state;

ALTER TABLE executions
    ADD CONSTRAINT chk_executions_audit_state
    CHECK (audit_state IN ('PENDING_AUDIT', 'FINALIZED', 'AUDIT_FAILED'));

COMMENT ON COLUMN executions.audit_state IS 'Saga state for the execution-finalization audit choreography: PENDING_AUDIT (finalized locally, waiting for system-events to record the audit log), FINALIZED (audit recorded, saga complete), AUDIT_FAILED (audit could not be recorded, compensated)';
