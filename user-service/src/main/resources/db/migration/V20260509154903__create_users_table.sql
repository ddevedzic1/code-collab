CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(300) NOT NULL,
    email VARCHAR(300) NOT NULL,
    password VARCHAR(300) NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(300),
    updated_at TIMESTAMP,
    updated_by VARCHAR(300)
);

COMMENT ON TABLE users IS 'Stores registered user accounts';
COMMENT ON COLUMN users.id IS 'Primary key, auto-generated UUID';
COMMENT ON COLUMN users.username IS 'Unique username chosen by the user during registration; uniqueness enforced only for active rows (end_date IS NULL) via a partial index';
COMMENT ON COLUMN users.email IS 'Email address used for login; uniqueness enforced only for active rows (end_date IS NULL) via a partial index';
COMMENT ON COLUMN users.password IS 'BCrypt-hashed password; never stored in plain text';
COMMENT ON COLUMN users.start_date IS 'Timestamp when this row became active';
COMMENT ON COLUMN users.end_date IS 'Timestamp when this row was soft-deleted; NULL means the row is active';
COMMENT ON COLUMN users.created_at IS 'Insert timestamp, set automatically by the database';
COMMENT ON COLUMN users.created_by IS 'Username of the principal that created the row';
COMMENT ON COLUMN users.updated_at IS 'Last update timestamp, maintained by the trg_users_set_updated_at trigger';
COMMENT ON COLUMN users.updated_by IS 'Username of the principal that last updated the row';

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username_active
    ON users (username)
    WHERE end_date IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_active
    ON users (email)
    WHERE end_date IS NULL;

CREATE OR REPLACE FUNCTION set_updated_at_to_now()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_users_set_updated_at ON users;
CREATE TRIGGER trg_users_set_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at_to_now();
