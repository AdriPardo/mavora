ALTER TABLE agent_runs
    ADD COLUMN created_at TIMESTAMPTZ;

UPDATE agent_runs SET created_at = started_at WHERE created_at IS NULL;

ALTER TABLE agent_runs
    ALTER COLUMN created_at SET NOT NULL;
