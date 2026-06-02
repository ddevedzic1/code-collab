-- Seed data for snippet-service.snippet_share_users
-- Run manually through DBeaver after snippet_share_seed.sql.
-- user_id is a logical reference to user-service.users.
-- Demonstrates per-user permission override: share is PUBLIC_LINK + READ_ONLY,
-- but user2 has EDIT permission via this row.

INSERT INTO snippet_service.snippet_share_users (id, snippet_share_id, user_id, permission) VALUES
    ('ffffffff-ffff-ffff-ffff-ffffffffffff',
     'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
     '22222222-2222-2222-2222-222222222222',
     'EDIT')
ON CONFLICT (id) DO NOTHING;
