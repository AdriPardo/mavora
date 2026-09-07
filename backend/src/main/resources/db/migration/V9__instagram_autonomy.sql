-- Instagram professional account, brand brief, media library and autonomous calendar.

CREATE TABLE brand_briefs (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    voice VARCHAR(500),
    offer VARCHAR(2000),
    cta VARCHAR(300),
    audience VARCHAR(500),
    extra_notes VARCHAR(4000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_brand_briefs_org ON brand_briefs (organization_id);

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    product_id UUID REFERENCES products (id),
    kind VARCHAR(16) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    storage_path VARCHAR(1024) NOT NULL,
    byte_size BIGINT NOT NULL,
    caption_hint VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_media_assets_org ON media_assets (organization_id);

CREATE TABLE instagram_accounts (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    provider VARCHAR(16) NOT NULL,
    ig_user_id VARCHAR(64) NOT NULL,
    username VARCHAR(120) NOT NULL,
    page_id VARCHAR(64),
    token_ciphertext TEXT,
    token_expires_at TIMESTAMPTZ,
    autonomy_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    connected_at TIMESTAMPTZ NOT NULL,
    disconnected_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_instagram_accounts_org ON instagram_accounts (organization_id);

CREATE TABLE instagram_slots (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organizations (id),
    format VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    hook VARCHAR(300) NOT NULL,
    caption VARCHAR(2200) NOT NULL,
    cta VARCHAR(300) NOT NULL,
    hashtags_json TEXT NOT NULL,
    media_asset_ids_json TEXT NOT NULL,
    ig_media_id VARCHAR(128),
    error_message VARCHAR(1000),
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_instagram_slots_org_sched ON instagram_slots (organization_id, scheduled_at);
CREATE INDEX ix_instagram_slots_due ON instagram_slots (status, scheduled_at);
