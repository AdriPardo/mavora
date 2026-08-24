package com.mavora.content.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ContentVariant {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID pieceId;
    private final String channel;
    private final String body;
    private final Instant createdAt;
    private final long version;

    private ContentVariant(
            UUID id, OrganizationId organizationId, UUID pieceId, String channel, String body,
            Instant createdAt, long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.pieceId = Objects.requireNonNull(pieceId);
        this.channel = Objects.requireNonNull(channel);
        this.body = Objects.requireNonNull(body);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static ContentVariant create(
            OrganizationId organizationId, UUID pieceId, String channel, String body, Instant now
    ) {
        return new ContentVariant(UUID.randomUUID(), organizationId, pieceId, channel, body, now, 0);
    }

    public static ContentVariant reconstitute(
            UUID id, OrganizationId organizationId, UUID pieceId, String channel, String body,
            Instant createdAt, long version
    ) {
        return new ContentVariant(id, organizationId, pieceId, channel, body, createdAt, version);
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID pieceId() {
        return pieceId;
    }

    public String channel() {
        return channel;
    }

    public String body() {
        return body;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
