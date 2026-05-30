CREATE TABLE IF NOT EXISTS snippet_share_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snippet_share_id UUID NOT NULL,
    user_id UUID NOT NULL,
    permission VARCHAR(30) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300),
    CONSTRAINT fk_snippet_share_users_snippet_shares FOREIGN KEY (snippet_share_id) REFERENCES snippet_shares (id),
    CONSTRAINT chk_snippet_share_users_permission CHECK (permission IN ('READ_ONLY', 'EDIT'))
);

COMMENT ON TABLE snippet_share_users IS 'Per-user access records for a snippet share; each user can have a permission override that takes precedence over snippet_shares.permission';
COMMENT ON COLUMN snippet_share_users.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN snippet_share_users.snippet_share_id IS 'Parent share; references snippet_shares.id within this service';
COMMENT ON COLUMN snippet_share_users.user_id IS 'Identifier of the granted user; logical reference to user-service users.id (no DB FK across services)';
COMMENT ON COLUMN snippet_share_users.permission IS 'Access level granted to this specific user; overrides snippet_shares.permission for this user, regardless of share_type';
COMMENT ON COLUMN snippet_share_users.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN snippet_share_users.end_date IS 'Timestamp when this row was soft-deleted; NULL means the user has active access';
COMMENT ON COLUMN snippet_share_users.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN snippet_share_users.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN snippet_share_users.updated_at IS 'Last update timestamp, maintained by the trg_snippet_share_users_set_updated_at trigger';
COMMENT ON COLUMN snippet_share_users.updated_by IS 'Username of the principal that last updated the row';

CREATE UNIQUE INDEX IF NOT EXISTS idx_snippet_share_users_share_user_active
    ON snippet_share_users (snippet_share_id, user_id)
    WHERE end_date IS NULL;

CREATE INDEX IF NOT EXISTS idx_snippet_share_users_user_id_active
    ON snippet_share_users (user_id)
    WHERE end_date IS NULL;

DROP TRIGGER IF EXISTS trg_snippet_share_users_set_updated_at ON snippet_share_users;
CREATE TRIGGER trg_snippet_share_users_set_updated_at
    BEFORE UPDATE ON snippet_share_users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
