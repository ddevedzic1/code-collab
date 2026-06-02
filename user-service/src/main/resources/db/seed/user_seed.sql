-- Seed data for user-service.users
-- Run manually through DBeaver after migrations have been applied.
-- BCrypt hash below corresponds to plain password "password".

INSERT INTO user_service.users (id, username, email, password) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ddevedzic1', 'ddevedzic1@etf.unsa.ba', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
    ('22222222-2222-2222-2222-222222222222', 'testuser',   'test@example.com',      '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT (id) DO NOTHING;
