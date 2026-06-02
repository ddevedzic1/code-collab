-- Seed data for snippet-service.snippet_shares
-- Run manually through DBeaver after snippet_seed.sql.

INSERT INTO snippet_service.snippet_shares (id, snippet_id, share_token, share_type, permission) VALUES
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     'seed-share-token-snippet1',
     'PUBLIC_LINK',
     'READ_ONLY')
ON CONFLICT (id) DO NOTHING;
