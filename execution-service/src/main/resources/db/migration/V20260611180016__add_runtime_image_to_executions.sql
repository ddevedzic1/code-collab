ALTER TABLE executions
    ADD COLUMN IF NOT EXISTS runtime_image VARCHAR(300);

COMMENT ON COLUMN executions.runtime_image IS 'Snapshot of the Docker image used to run the code (e.g. python:3.11-slim), captured from snippet-service languages.runtime_image at submission time; preserves the runtime even if the language image is later changed';
