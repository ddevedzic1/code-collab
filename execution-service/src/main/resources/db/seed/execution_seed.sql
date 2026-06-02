-- Seed data for execution-service.executions
-- Run manually through DBeaver after migrations have been applied.
-- snippet_id, user_id and language_id are logical references to snippet-service
-- and user-service tables; values match those used in user_seed.sql,
-- language_seed.sql and snippet_seed.sql so cross-service joins line up.
-- execution_queue is intentionally not seeded.

INSERT INTO execution_service.executions
    (id, snippet_id, user_id, language_id, code_snapshot, status, stdout, stderr, exit_code, duration_ms)
VALUES
    ('a0000001-0000-0000-0000-000000000001',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     '11111111-1111-1111-1111-111111111111',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     E'print(''Hello, World!'')\n',
     'COMPLETED',
     E'Hello, World!\n',
     NULL,
     0,
     742),

    ('b0000002-0000-0000-0000-000000000002',
     'dddddddd-dddd-dddd-dddd-dddddddddddd',
     '22222222-2222-2222-2222-222222222222',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     E'console.log(''Hello, World!'');\nthrow new Error(''boom'');\n',
     'FAILED',
     NULL,
     E'Error: boom\n    at Object.<anonymous>\n',
     1,
     1158)
ON CONFLICT (id) DO NOTHING;
