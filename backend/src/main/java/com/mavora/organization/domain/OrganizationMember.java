package com.mavora.organization.domain;

import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class OrganizationMember {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UserId userId;
    private final OrganizationRole role;
    private final Instant createdAt;
    private long version;

    private OrganizationMember(
            UUID id,
            OrganizationId organizationId,
            UserId userId,
            OrganizationRole role,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.userId = Objects.requireNonNull(userId);
        this.role = Objects.requireNonNull(role);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static OrganizationMember create(
            OrganizationId organizationId,
            UserId userId,
            OrganizationRole role,
            Instant now
    ) {
        return new OrganizationMember(UUID.randomUUID(), organizationId, userId, role, now, 0);
    }

    public static OrganizationMember reconstitute(
            UUID id,
            OrganizationId organizationId,
            UserId userId,
            OrganizationRole role,
            Instant createdAt,
            long version
    ) {
        return new OrganizationMember(id, organizationId, userId, role, createdAt, version);
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UserId userId() {
        return userId;
    }

    public OrganizationRole role() {
        return role;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
