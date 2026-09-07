-- Per-organization Meta app credentials for Instagram OAuth. Secret is stored encrypted.

CREATE TABLE meta_connection_settings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    app_id VARCHAR(64),
    app_secret_ciphertext TEXT,
    redirect_uri VARCHAR(500),
    graph_version VARCHAR(16) NOT NULL DEFAULT 'v21.0',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_meta_connection_settings_org ON meta_connection_settings (organization_id);
