INSERT INTO languages (code, name, version, runtime_image)
SELECT 'python', 'Python', '3.11', 'python:3.11-slim'
WHERE NOT EXISTS (
    SELECT 1 FROM languages
    WHERE code = 'python' AND (end_date IS NULL OR end_date > NOW())
);
