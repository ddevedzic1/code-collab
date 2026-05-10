CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS languages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(30) NOT NULL,
    name VARCHAR(300) NOT NULL,
    version VARCHAR(30) NOT NULL,
    runtime_image VARCHAR(300) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300)
);

COMMENT ON TABLE languages IS 'Programming languages available for snippet creation and execution; managed by administrators';
COMMENT ON COLUMN languages.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN languages.code IS 'Stable slug identifier (e.g., python311, node20); unique among active rows enforced by trg_languages_unique_active_code';
COMMENT ON COLUMN languages.name IS 'Human-readable language name (e.g., Python, JavaScript)';
COMMENT ON COLUMN languages.version IS 'Language or runtime version label (e.g., 3.11, 20.x)';
COMMENT ON COLUMN languages.runtime_image IS 'Docker image used to execute snippets in this language (e.g., python:3.11-slim)';
COMMENT ON COLUMN languages.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN languages.end_date IS 'Timestamp when this row was soft-deleted; NULL or future value means the row is active';
COMMENT ON COLUMN languages.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN languages.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN languages.updated_at IS 'Last update timestamp, maintained by the trg_languages_set_updated_at trigger';
COMMENT ON COLUMN languages.updated_by IS 'Username of the principal that last updated the row';

CREATE OR REPLACE FUNCTION set_updated_at_to_now()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_languages_set_updated_at ON languages;
CREATE TRIGGER trg_languages_set_updated_at
    BEFORE UPDATE ON languages
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();

CREATE OR REPLACE FUNCTION languages_check_unique_active_code()
RETURNS TRIGGER AS $$
BEGIN
    EXECUTE format(
        'SELECT 1 FROM %I.%I WHERE code = $1 AND id <> $2 AND (end_date IS NULL OR end_date > NOW()) LIMIT 1',
        TG_TABLE_SCHEMA, TG_TABLE_NAME
    )
    USING NEW.code, NEW.id;

    IF FOUND THEN
        RAISE EXCEPTION 'Duplicate active language code: %', NEW.code
            USING ERRCODE = 'unique_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_languages_unique_active_code ON languages;
CREATE TRIGGER trg_languages_unique_active_code
    BEFORE INSERT OR UPDATE OF code, end_date ON languages
    FOR EACH ROW
    EXECUTE FUNCTION languages_check_unique_active_code();
