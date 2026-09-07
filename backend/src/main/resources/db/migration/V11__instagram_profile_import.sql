ALTER TABLE instagram_accounts
    ADD COLUMN import_summary VARCHAR(500),
    ADD COLUMN import_fields VARCHAR(500),
    ADD COLUMN imported_at TIMESTAMPTZ;
