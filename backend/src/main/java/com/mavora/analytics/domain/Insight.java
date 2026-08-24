package com.mavora.analytics.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Insight {
    private final UUID id;
    private final OrganizationId organizationId;
    private final String title;
    private final String body;
    private final Instant createdAt;
    private final long version;

    private Insight(UUID id, OrganizationId organizationId, String title, String body, Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Insight create(OrganizationId organizationId, String title, String body, Instant now) {
        return new Insight(UUID.randomUUID(), organizationId, title, body, now, 0);
    }

    public static Insight reconstitute(UUID id, OrganizationId organizationId, String title, String body,
                                       Instant createdAt, long version) {
        return new Insight(id, organizationId, title, body, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public String title() { return title; }
    public String body() { return body; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
