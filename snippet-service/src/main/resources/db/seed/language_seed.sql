-- Seed data for snippet-service.languages
-- Run manually through DBeaver after migrations have been applied.

INSERT INTO snippet_service.languages (id, code, name, version, runtime_image) VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'python311', 'Python',  '3.11', 'python:3.11-slim'),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'node20',    'Node.js', '20.x', 'node:20-alpine')
ON CONFLICT (id) DO NOTHING;
