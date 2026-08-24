package com.mavora.content.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ContentIdea {

    private final UUID id;
    private final OrganizationId organizationId;
    private final String title;
    private final String angle;
    private final String pillar;
    private final String status;
    private final Instant createdAt;
    private final long version;

    private ContentIdea(
            UUID id,
            OrganizationId organizationId,
            String title,
            String angle,
            String pillar,
            String status,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.title = Objects.requireNonNull(title);
        this.angle = Objects.requireNonNull(angle);
        this.pillar = pillar;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static ContentIdea create(
            OrganizationId organizationId, String title, String angle, String pillar, Instant now
    ) {
        return new ContentIdea(UUID.randomUUID(), organizationId, title, angle, pillar, "CAPTURED", now, 0);
    }

    public static ContentIdea reconstitute(
            UUID id, OrganizationId organizationId, String title, String angle, String pillar,
            String status, Instant createdAt, long version
    ) {
        return new ContentIdea(id, organizationId, title, angle, pillar, status, createdAt, version);
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String title() {
        return title;
    }

    public String angle() {
        return angle;
    }

    public String pillar() {
        return pillar;
    }

    public String status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
