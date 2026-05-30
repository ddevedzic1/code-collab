CREATE TABLE IF NOT EXISTS snippet_shares (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snippet_id UUID NOT NULL,
    share_token VARCHAR(300) NOT NULL,
    share_type VARCHAR(30) NOT NULL,
    permission VARCHAR(30) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300),
    CONSTRAINT fk_snippet_shares_snippets FOREIGN KEY (snippet_id) REFERENCES snippets (id),
    CONSTRAINT chk_snippet_shares_share_type CHECK (share_type IN ('PUBLIC_LINK', 'USER')),
    CONSTRAINT chk_snippet_shares_permission CHECK (permission IN ('READ_ONLY', 'EDIT'))
);

COMMENT ON TABLE snippet_shares IS 'Defines how a snippet is shared; one active row per snippet';
COMMENT ON COLUMN snippet_shares.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN snippet_shares.snippet_id IS 'Snippet being shared; references snippets.id within this service';
COMMENT ON COLUMN snippet_shares.share_token IS 'Stable token used in shareable URLs; never changes once issued; unique among active rows';
COMMENT ON COLUMN snippet_shares.share_type IS 'Distribution mode: PUBLIC_LINK (anyone with token) or USER (only listed users)';
COMMENT ON COLUMN snippet_shares.permission IS 'Default access level granted to whoever can reach the snippet via this share; can be overridden per user in snippet_share_users';
COMMENT ON COLUMN snippet_shares.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN snippet_shares.end_date IS 'Timestamp when this row was soft-deleted; NULL means the row is active';
COMMENT ON COLUMN snippet_shares.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN snippet_shares.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN snippet_shares.updated_at IS 'Last update timestamp, maintained by the trg_snippet_shares_set_updated_at trigger';
COMMENT ON COLUMN snippet_shares.updated_by IS 'Username of the principal that last updated the row';

CREATE UNIQUE INDEX IF NOT EXISTS idx_snippet_shares_snippet_id_active
    ON snippet_shares (snippet_id)
    WHERE end_date IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_snippet_shares_share_token_active
    ON snippet_shares (share_token)
    WHERE end_date IS NULL;

DROP TRIGGER IF EXISTS trg_snippet_shares_set_updated_at ON snippet_shares;
CREATE TRIGGER trg_snippet_shares_set_updated_at
    BEFORE UPDATE ON snippet_shares
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
