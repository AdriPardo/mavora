package com.mavora.strategy.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class Campaign {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID strategyId;
    private final String name;
    private final CampaignStatus status;
    private final String channelsJson;
    private final Instant createdAt;
    private final long version;

    private Campaign(
            UUID id,
            OrganizationId organizationId,
            UUID strategyId,
            String name,
            CampaignStatus status,
            String channelsJson,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.strategyId = Objects.requireNonNull(strategyId);
        this.name = Objects.requireNonNull(name);
        this.status = Objects.requireNonNull(status);
        this.channelsJson = Objects.requireNonNull(channelsJson);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Campaign fromApprovedStrategy(MarketingStrategy strategy, Instant now) {
        return new Campaign(
                UUID.randomUUID(),
                strategy.organizationId(),
                strategy.id(),
                "Campaña · " + strategy.status().name(),
                CampaignStatus.ACTIVE,
                strategy.channelsJson(),
                now,
                0
        );
    }

    public static Campaign create(
            OrganizationId organizationId,
            UUID strategyId,
            String name,
            String channelsJson,
            Instant now
    ) {
        return new Campaign(
                UUID.randomUUID(), organizationId, strategyId, name, CampaignStatus.ACTIVE, channelsJson, now, 0
        );
    }

    public static Campaign reconstitute(
            UUID id,
            OrganizationId organizationId,
            UUID strategyId,
            String name,
            CampaignStatus status,
            String channelsJson,
            Instant createdAt,
            long version
    ) {
        return new Campaign(id, organizationId, strategyId, name, status, channelsJson, createdAt, version);
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID strategyId() {
        return strategyId;
    }

    public String name() {
        return name;
    }

    public CampaignStatus status() {
        return status;
    }

    public String channelsJson() {
        return channelsJson;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
