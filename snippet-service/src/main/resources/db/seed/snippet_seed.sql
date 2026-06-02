-- Seed data for snippet-service.snippets
-- Run manually through DBeaver after language_seed.sql.
-- user_id values are logical references to user-service.users.

INSERT INTO snippet_service.snippets (id, user_id, language_id, title, content) VALUES
    ('cccccccc-cccc-cccc-cccc-cccccccccccc',
     '11111111-1111-1111-1111-111111111111',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     'Hello World in Python',
     E'print(''Hello, World!'')\n'),

    ('dddddddd-dddd-dddd-dddd-dddddddddddd',
     '22222222-2222-2222-2222-222222222222',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     'Hello World in Node.js',
     E'console.log(''Hello, World!'');\n')
ON CONFLICT (id) DO NOTHING;
