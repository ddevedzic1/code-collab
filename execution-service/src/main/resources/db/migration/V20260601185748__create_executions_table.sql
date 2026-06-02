CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snippet_id UUID NOT NULL,
    user_id UUID NOT NULL,
    language_id UUID NOT NULL,
    code_snapshot TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    stdout TEXT,
    stderr TEXT,
    exit_code INT,
    duration_ms INT,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300),
    CONSTRAINT chk_executions_status CHECK (status IN ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE executions IS 'User-submitted code execution requests with their results';
COMMENT ON COLUMN executions.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN executions.snippet_id IS 'Snippet that was executed; logical reference to snippet-service snippets.id (no DB FK across services)';
COMMENT ON COLUMN executions.user_id IS 'User who submitted the execution; logical reference to user-service users.id (no DB FK across services)';
COMMENT ON COLUMN executions.language_id IS 'Language used to execute the code; logical reference to snippet-service languages.id (no DB FK across services)';
COMMENT ON COLUMN executions.code_snapshot IS 'Snapshot of the source code at submission time; preserves history if snippet is later modified';
COMMENT ON COLUMN executions.status IS 'Lifecycle state from the user perspective: PENDING, RUNNING, COMPLETED, FAILED';
COMMENT ON COLUMN executions.stdout IS 'Standard output captured during execution; NULL until execution completes';
COMMENT ON COLUMN executions.stderr IS 'Standard error captured during execution; NULL until execution completes';
COMMENT ON COLUMN executions.exit_code IS 'Process exit code; NULL until execution completes';
COMMENT ON COLUMN executions.duration_ms IS 'Wall-clock execution time in milliseconds; NULL until execution completes';
COMMENT ON COLUMN executions.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN executions.end_date IS 'Timestamp when this row was soft-deleted; NULL means the row is active';
COMMENT ON COLUMN executions.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN executions.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN executions.updated_at IS 'Last update timestamp, maintained by the trg_executions_set_updated_at trigger';
COMMENT ON COLUMN executions.updated_by IS 'Username of the principal that last updated the row';

CREATE INDEX IF NOT EXISTS idx_executions_user_id ON executions (user_id);
CREATE INDEX IF NOT EXISTS idx_executions_snippet_id ON executions (snippet_id);
CREATE INDEX IF NOT EXISTS idx_executions_status ON executions (status);

CREATE OR REPLACE FUNCTION set_updated_at_to_now()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_executions_set_updated_at ON executions;
CREATE TRIGGER trg_executions_set_updated_at
    BEFORE UPDATE ON executions
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
