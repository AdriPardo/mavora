package com.mavora.analytics.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Learning {
    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID insightId;
    private final String title;
    private final String body;
    private final Instant createdAt;
    private final long version;

    private Learning(UUID id, OrganizationId organizationId, UUID insightId, String title, String body,
                     Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.insightId = insightId;
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Learning create(OrganizationId organizationId, UUID insightId, String title, String body, Instant now) {
        return new Learning(UUID.randomUUID(), organizationId, insightId, title, body, now, 0);
    }

    public static Learning reconstitute(UUID id, OrganizationId organizationId, UUID insightId, String title,
                                        String body, Instant createdAt, long version) {
        return new Learning(id, organizationId, insightId, title, body, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public UUID insightId() { return insightId; }
    public String title() { return title; }
    public String body() { return body; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
