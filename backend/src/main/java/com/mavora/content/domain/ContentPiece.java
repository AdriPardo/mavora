package com.mavora.content.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ContentPiece {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID ideaId;
    private final String title;
    private final String body;
    private PieceStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private ContentPiece(
            UUID id,
            OrganizationId organizationId,
            UUID ideaId,
            String title,
            String body,
            PieceStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.ideaId = Objects.requireNonNull(ideaId);
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static ContentPiece pending(
            OrganizationId organizationId, UUID ideaId, String title, String body, Instant now
    ) {
        return new ContentPiece(
                UUID.randomUUID(), organizationId, ideaId, title, body, PieceStatus.PENDING_APPROVAL, now, now, 0
        );
    }

    public static ContentPiece reconstitute(
            UUID id, OrganizationId organizationId, UUID ideaId, String title, String body,
            PieceStatus status, Instant createdAt, Instant updatedAt, long version
    ) {
        return new ContentPiece(id, organizationId, ideaId, title, body, status, createdAt, updatedAt, version);
    }

    public void approve(Instant now) {
        if (status != PieceStatus.PENDING_APPROVAL) {
            throw new DomainException("Only pending content can be approved");
        }
        this.status = PieceStatus.APPROVED;
        this.updatedAt = now;
    }

    public void reject(Instant now) {
        if (status != PieceStatus.PENDING_APPROVAL) {
            throw new DomainException("Only pending content can be rejected");
        }
        this.status = PieceStatus.REJECTED;
        this.updatedAt = now;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID ideaId() {
        return ideaId;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public PieceStatus status() {
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
