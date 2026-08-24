package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class IcpProfile {
    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID strategyId;
    private final String summary;
    private final String segmentsJson;
    private final Instant createdAt;
    private final long version;

    private IcpProfile(UUID id, OrganizationId organizationId, UUID strategyId, String summary,
                       String segmentsJson, Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.strategyId = Objects.requireNonNull(strategyId);
        this.summary = Objects.requireNonNull(summary);
        this.segmentsJson = Objects.requireNonNull(segmentsJson);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static IcpProfile create(OrganizationId organizationId, UUID strategyId, String summary,
                                    String segmentsJson, Instant now) {
        return new IcpProfile(UUID.randomUUID(), organizationId, strategyId, summary, segmentsJson, now, 0);
    }

    public static IcpProfile reconstitute(UUID id, OrganizationId organizationId, UUID strategyId, String summary,
                                          String segmentsJson, Instant createdAt, long version) {
        return new IcpProfile(id, organizationId, strategyId, summary, segmentsJson, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public UUID strategyId() { return strategyId; }
    public String summary() { return summary; }
    public String segmentsJson() { return segmentsJson; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
