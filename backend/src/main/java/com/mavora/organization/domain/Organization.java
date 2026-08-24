package com.mavora.organization.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;

public final class Organization {

    private final OrganizationId id;
    private final String name;
    private final String slug;
    private final OrganizationPlan plan;
    private final OrganizationStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Organization(
            OrganizationId id,
            String name,
            String slug,
            OrganizationPlan plan,
            OrganizationStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireName(name);
        this.slug = requireSlug(slug);
        this.plan = Objects.requireNonNull(plan);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static Organization create(
            OrganizationId id,
            String name,
            String slug,
            Instant now
    ) {
        return new Organization(id, name, slug, OrganizationPlan.TRIAL, OrganizationStatus.ACTIVE, now, now, 0);
    }

    public static Organization reconstitute(
            OrganizationId id,
            String name,
            String slug,
            OrganizationPlan plan,
            OrganizationStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new Organization(id, name, slug, plan, status, createdAt, updatedAt, version);
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

    private static String requireSlug(String slug) {
        if (slug == null || slug.isBlank() || slug.length() > 80) {
            throw new IllegalArgumentException("slug is invalid");
        }
        return slug;
    }

    public OrganizationId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String slug() {
        return slug;
    }

    public OrganizationPlan plan() {
        return plan;
    }

    public OrganizationStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public long version() {
        return version;
    }
}
