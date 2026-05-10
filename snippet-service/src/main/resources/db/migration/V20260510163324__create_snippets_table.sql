CREATE TABLE IF NOT EXISTS snippets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    language_id UUID NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300),
    CONSTRAINT fk_snippets_languages FOREIGN KEY (language_id) REFERENCES languages (id)
);

COMMENT ON TABLE snippets IS 'User-created code snippets, scoped to a specific programming language';
COMMENT ON COLUMN snippets.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN snippets.user_id IS 'Identifier of the owning user; logical reference to user-service users.id (no DB FK across services)';
COMMENT ON COLUMN snippets.language_id IS 'Programming language used by this snippet; references languages.id within this service';
COMMENT ON COLUMN snippets.title IS 'Human-readable snippet title shown in listings';
COMMENT ON COLUMN snippets.content IS 'Raw source code submitted by the user';
COMMENT ON COLUMN snippets.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN snippets.end_date IS 'Timestamp when this row was soft-deleted; NULL or future value means the row is active';
COMMENT ON COLUMN snippets.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN snippets.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN snippets.updated_at IS 'Last update timestamp, maintained by the trg_snippets_set_updated_at trigger';
COMMENT ON COLUMN snippets.updated_by IS 'Username of the principal that last updated the row';

CREATE INDEX IF NOT EXISTS idx_snippets_user_id ON snippets (user_id);
CREATE INDEX IF NOT EXISTS idx_snippets_language_id ON snippets (language_id);

DROP TRIGGER IF EXISTS trg_snippets_set_updated_at ON snippets;
CREATE TRIGGER trg_snippets_set_updated_at
    BEFORE UPDATE ON snippets
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
