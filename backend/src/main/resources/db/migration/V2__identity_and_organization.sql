-- Identity, tenancy and audit for the first authenticated slice.

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_users_email ON users (email);

CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    plan VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_organizations_slug ON organizations (slug);

CREATE TABLE organization_members (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    user_id UUID NOT NULL REFERENCES users (id),
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_org_members_org_user ON organization_members (organization_id, user_id);
CREATE INDEX ix_org_members_user ON organization_members (user_id);
CREATE INDEX ix_org_members_org ON organization_members (organization_id);

CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id),
    token_hash CHAR(64) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    ip VARCHAR(64),
    user_agent VARCHAR(256)
);

CREATE UNIQUE INDEX uk_sessions_token_hash ON sessions (token_hash);
CREATE INDEX ix_sessions_user ON sessions (user_id);
CREATE INDEX ix_sessions_expires ON sessions (expires_at);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    organization_id UUID,
    actor_user_id UUID,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(64),
    resource_id UUID,
    metadata TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    ip VARCHAR(64)
);

CREATE INDEX ix_audit_org ON audit_events (organization_id);
CREATE INDEX ix_audit_actor ON audit_events (actor_user_id);
CREATE INDEX ix_audit_created ON audit_events (created_at);
