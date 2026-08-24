CREATE TABLE marketing_strategies (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    company_id UUID NOT NULL REFERENCES companies (id),
    goal_id UUID NOT NULL REFERENCES marketing_goals (id),
    workflow_id UUID REFERENCES workflow_executions (id),
    status VARCHAR(32) NOT NULL,
    positioning TEXT NOT NULL,
    icp_summary TEXT NOT NULL,
    channels_json TEXT NOT NULL,
    pillars_json TEXT NOT NULL,
    kpis_json TEXT NOT NULL,
    narrative TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_strategy_org ON marketing_strategies (organization_id);
CREATE INDEX ix_strategy_org_status ON marketing_strategies (organization_id, status);

CREATE TABLE approval_requests (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    request_type VARCHAR(64) NOT NULL,
    subject_type VARCHAR(64) NOT NULL,
    subject_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    payload_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    decided_at TIMESTAMPTZ,
    decided_by UUID REFERENCES users (id),
    decision_note VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_approval_org ON approval_requests (organization_id);
CREATE INDEX ix_approval_org_status ON approval_requests (organization_id, status);

CREATE TABLE campaigns (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    strategy_id UUID NOT NULL REFERENCES marketing_strategies (id),
    name VARCHAR(160) NOT NULL,
    status VARCHAR(32) NOT NULL,
    channels_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_campaigns_org ON campaigns (organization_id);
