package com.mavora.company.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Product {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID companyId;
    private final String name;
    private final String description;
    private final String url;
    private final Instant createdAt;
    private final long version;

    private Product(
            UUID id,
            OrganizationId organizationId,
            UUID companyId,
            String name,
            String description,
            String url,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.companyId = Objects.requireNonNull(companyId);
        this.name = requireName(name);
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.url = HttpUrl.normalizeOptional(url).orElse(null);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
        if (this.description != null && this.description.length() > 2000) {
            throw new IllegalArgumentException("description is too long");
        }
    }

    public static Product create(
            OrganizationId organizationId,
            UUID companyId,
            String name,
            String description,
            String url,
            Instant now
    ) {
        return new Product(UUID.randomUUID(), organizationId, companyId, name, description, url, now, 0);
    }

    public static Product reconstitute(
            UUID id,
            OrganizationId organizationId,
            UUID companyId,
            String name,
            String description,
            String url,
            Instant createdAt,
            long version
    ) {
        return new Product(id, organizationId, companyId, name, description, url, createdAt, version);
    }

    private static String requireName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2 || trimmed.length() > 120) {
            throw new IllegalArgumentException("name must be between 2 and 120 characters");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID companyId() {
        return companyId;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String url() {
        return url;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
