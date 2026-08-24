package com.mavora.social.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Publication {
    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID pieceId;
    private final String channel;
    private final String copy;
    private PublicationStatus status;
    private Instant publishedAt;
    private final Instant createdAt;
    private long version;

    private Publication(UUID id, OrganizationId organizationId, UUID pieceId, String channel, String copy,
                        PublicationStatus status, Instant publishedAt, Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.pieceId = pieceId;
        this.channel = Objects.requireNonNull(channel);
        this.copy = Objects.requireNonNull(copy);
        this.status = Objects.requireNonNull(status);
        this.publishedAt = publishedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Publication draft(OrganizationId organizationId, UUID pieceId, String channel, String copy, Instant now) {
        return new Publication(UUID.randomUUID(), organizationId, pieceId, channel, copy, PublicationStatus.DRAFT, null, now, 0);
    }

    public static Publication reconstitute(UUID id, OrganizationId organizationId, UUID pieceId, String channel, String copy,
                                           PublicationStatus status, Instant publishedAt, Instant createdAt, long version) {
        return new Publication(id, organizationId, pieceId, channel, copy, status, publishedAt, createdAt, version);
    }

    public void publish(Instant now) {
        if (status != PublicationStatus.DRAFT) {
            throw new DomainException("Only a draft publication can be published");
        }
        this.status = PublicationStatus.PUBLISHED;
        this.publishedAt = now;
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public UUID pieceId() { return pieceId; }
    public String channel() { return channel; }
    public String copy() { return copy; }
    public PublicationStatus status() { return status; }
    public Instant publishedAt() { return publishedAt; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
