CREATE TABLE personas (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    strategy_id UUID NOT NULL REFERENCES marketing_strategies (id),
    name VARCHAR(120) NOT NULL,
    summary TEXT NOT NULL,
    pains TEXT NOT NULL,
    jobs TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_personas_org ON personas (organization_id);

CREATE TABLE competitors (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    name VARCHAR(160) NOT NULL,
    url VARCHAR(2048),
    notes TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_competitors_org ON competitors (organization_id);

CREATE TABLE icp_profiles (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    strategy_id UUID NOT NULL REFERENCES marketing_strategies (id),
    summary TEXT NOT NULL,
    segments_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_icp_org ON icp_profiles (organization_id);

CREATE TABLE content_ideas (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    title VARCHAR(200) NOT NULL,
    angle TEXT NOT NULL,
    pillar VARCHAR(120),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_ideas_org ON content_ideas (organization_id);

CREATE TABLE content_pieces (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    idea_id UUID NOT NULL REFERENCES content_ideas (id),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_pieces_org ON content_pieces (organization_id);

CREATE TABLE content_variants (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    piece_id UUID NOT NULL REFERENCES content_pieces (id),
    channel VARCHAR(64) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_variants_org ON content_variants (organization_id);
CREATE INDEX ix_variants_piece ON content_variants (piece_id);

CREATE TABLE publications (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    piece_id UUID REFERENCES content_pieces (id),
    channel VARCHAR(64) NOT NULL,
    copy TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_publications_org ON publications (organization_id);

CREATE TABLE metric_snapshots (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    metric VARCHAR(80) NOT NULL,
    value BIGINT NOT NULL,
    captured_at TIMESTAMPTZ NOT NULL,
    source VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_snapshots_org ON metric_snapshots (organization_id);
CREATE INDEX ix_snapshots_org_metric ON metric_snapshots (organization_id, metric);

CREATE TABLE insights (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_insights_org ON insights (organization_id);

CREATE TABLE learnings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    insight_id UUID REFERENCES insights (id),
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_learnings_org ON learnings (organization_id);
