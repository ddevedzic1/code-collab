CREATE TABLE IF NOT EXISTS execution_queue (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    execution_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    priority INT NOT NULL DEFAULT 0,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300),
    CONSTRAINT fk_execution_queue_executions FOREIGN KEY (execution_id) REFERENCES executions (id),
    CONSTRAINT chk_execution_queue_status CHECK (status IN ('WAITING', 'PROCESSING', 'DONE'))
);

COMMENT ON TABLE execution_queue IS 'Queue tracking for executions; mirrors RabbitMQ delivery state and exposes priority and queue status for admin visibility';
COMMENT ON COLUMN execution_queue.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN execution_queue.execution_id IS 'Execution this queue entry tracks; references executions.id within this service';
COMMENT ON COLUMN execution_queue.status IS 'Queue lifecycle from the system perspective: WAITING (enqueued), PROCESSING (worker picked up), DONE (worker finished regardless of execution outcome)';
COMMENT ON COLUMN execution_queue.priority IS 'Priority for ordering; higher value processed first; 0 = default';
COMMENT ON COLUMN execution_queue.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN execution_queue.end_date IS 'Timestamp when this row was soft-deleted; NULL means the row is active';
COMMENT ON COLUMN execution_queue.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN execution_queue.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN execution_queue.updated_at IS 'Last update timestamp, maintained by the trg_execution_queue_set_updated_at trigger';
COMMENT ON COLUMN execution_queue.updated_by IS 'Username of the principal that last updated the row';

CREATE INDEX IF NOT EXISTS idx_execution_queue_execution_id ON execution_queue (execution_id);
CREATE INDEX IF NOT EXISTS idx_execution_queue_status ON execution_queue (status);

DROP TRIGGER IF EXISTS trg_execution_queue_set_updated_at ON execution_queue;
CREATE TRIGGER trg_execution_queue_set_updated_at
    BEFORE UPDATE ON execution_queue
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
