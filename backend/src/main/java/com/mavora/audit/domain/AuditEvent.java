package com.mavora.audit.domain;

import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record AuditEvent(
        UUID id,
        OrganizationId organizationId,
        UserId actorUserId,
        String action,
        String resourceType,
        UUID resourceId,
        String metadata,
        Instant createdAt,
        String ip
) {

    public static AuditEvent of(
            OrganizationId organizationId,
            UserId actorUserId,
            String action,
            String resourceType,
            UUID resourceId,
            String metadata,
            Instant createdAt,
            String ip
    ) {
        return new AuditEvent(
                UUID.randomUUID(),
                organizationId,
                actorUserId,
                action,
                resourceType,
                resourceId,
                metadata,
                createdAt,
                ip
        );
    }

    public Optional<OrganizationId> organization() {
        return Optional.ofNullable(organizationId);
    }

    public Optional<UserId> actor() {
        return Optional.ofNullable(actorUserId);
    }
}
