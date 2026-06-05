CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS system_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    microservice VARCHAR(300) NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    resource VARCHAR(300) NOT NULL,
    response_type VARCHAR(30) NOT NULL,
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    CONSTRAINT chk_system_events_action_type CHECK (action_type IN ('CREATE', 'UPDATE', 'DELETE', 'GET', 'EXECUTE')),
    CONSTRAINT chk_system_events_response_type CHECK (response_type IN ('SUCCESS', 'ERROR'))
);

COMMENT ON TABLE system_events IS 'Append-only audit trail of significant actions reported by other microservices; never updated or deleted';
COMMENT ON COLUMN system_events.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN system_events.user_id IS 'User who triggered the action; logical reference to user-service users.id (no DB FK across services); NULL for system-initiated events';
COMMENT ON COLUMN system_events.microservice IS 'Name of the microservice that produced the event (e.g. execution-service)';
COMMENT ON COLUMN system_events.action_type IS 'Kind of action: CREATE, UPDATE, DELETE, GET or EXECUTE';
COMMENT ON COLUMN system_events.resource IS 'Identifier of the affected resource, typically the UUID of the domain entity (e.g. the execution id)';
COMMENT ON COLUMN system_events.response_type IS 'Outcome of the action: SUCCESS or ERROR';
COMMENT ON COLUMN system_events.details IS 'Optional free-form or JSON details about the event';
COMMENT ON COLUMN system_events.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN system_events.created_by IS 'Username of the principal that produced the event; NULL when not provided';

CREATE INDEX IF NOT EXISTS idx_system_events_user_id ON system_events (user_id);
CREATE INDEX IF NOT EXISTS idx_system_events_resource ON system_events (resource);

CREATE UNIQUE INDEX IF NOT EXISTS idx_system_events_execute_resource_unique
    ON system_events (resource, action_type)
    WHERE action_type = 'EXECUTE';
