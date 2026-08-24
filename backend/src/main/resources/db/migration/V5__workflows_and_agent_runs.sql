-- Durable job runtime. Poller claims QUEUED rows with SKIP LOCKED.

CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    workflow_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    input_json TEXT,
    output_json TEXT,
    error_message VARCHAR(1000),
    created_by UUID NOT NULL REFERENCES users (id),
    created_at TIMESTAMPTZ NOT NULL,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_wf_org ON workflow_executions (organization_id);
CREATE INDEX ix_wf_org_type_status ON workflow_executions (organization_id, workflow_type, status);
CREATE INDEX ix_wf_queued ON workflow_executions (status, created_at);

CREATE TABLE agent_runs (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    workflow_id UUID NOT NULL REFERENCES workflow_executions (id),
    agent_type VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    model VARCHAR(120),
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    cost_cents BIGINT NOT NULL DEFAULT 0,
    input_json TEXT,
    output_json TEXT,
    error_message VARCHAR(1000),
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_runs_org ON agent_runs (organization_id);
CREATE INDEX ix_runs_workflow ON agent_runs (workflow_id);
CREATE INDEX ix_runs_org_started ON agent_runs (organization_id, started_at);
