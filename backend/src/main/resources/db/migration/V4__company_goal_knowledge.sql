-- Company profile, products, marketing goals and persistent knowledge.

CREATE TABLE companies (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    name VARCHAR(120) NOT NULL,
    website_url VARCHAR(2048),
    description VARCHAR(4000),
    market VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_companies_org ON companies (organization_id);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    company_id UUID NOT NULL REFERENCES companies (id),
    name VARCHAR(120) NOT NULL,
    description VARCHAR(2000),
    url VARCHAR(2048),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_products_org ON products (organization_id);
CREATE INDEX ix_products_company ON products (company_id);

CREATE TABLE marketing_goals (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    metric VARCHAR(80) NOT NULL,
    target_value BIGINT NOT NULL,
    deadline DATE NOT NULL,
    budget_cents BIGINT NOT NULL,
    budget_currency VARCHAR(3) NOT NULL,
    market VARCHAR(200) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_goals_org ON marketing_goals (organization_id);
CREATE INDEX ix_goals_org_status ON marketing_goals (organization_id, status);

CREATE TABLE knowledge_items (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    kind VARCHAR(32) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    source VARCHAR(2048),
    confidence INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_knowledge_org ON knowledge_items (organization_id);
CREATE INDEX ix_knowledge_org_kind ON knowledge_items (organization_id, kind);
