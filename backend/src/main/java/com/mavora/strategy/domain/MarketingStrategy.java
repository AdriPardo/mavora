package com.mavora.strategy.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MarketingStrategy {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID companyId;
    private final UUID goalId;
    private final UUID workflowId;
    private StrategyStatus status;
    private final String positioning;
    private final String icpSummary;
    private final String channelsJson;
    private final String pillarsJson;
    private final String kpisJson;
    private final String narrative;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private MarketingStrategy(
            UUID id,
            OrganizationId organizationId,
            UUID companyId,
            UUID goalId,
            UUID workflowId,
            StrategyStatus status,
            String positioning,
            String icpSummary,
            String channelsJson,
            String pillarsJson,
            String kpisJson,
            String narrative,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.companyId = Objects.requireNonNull(companyId);
        this.goalId = Objects.requireNonNull(goalId);
        this.workflowId = workflowId;
        this.status = Objects.requireNonNull(status);
        this.positioning = requireText(positioning, "positioning");
        this.icpSummary = requireText(icpSummary, "icpSummary");
        this.channelsJson = Objects.requireNonNull(channelsJson);
        this.pillarsJson = Objects.requireNonNull(pillarsJson);
        this.kpisJson = Objects.requireNonNull(kpisJson);
        this.narrative = requireText(narrative, "narrative");
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static MarketingStrategy draft(
            OrganizationId organizationId,
            UUID companyId,
            UUID goalId,
            UUID workflowId,
            String positioning,
            String icpSummary,
            String channelsJson,
            String pillarsJson,
            String kpisJson,
            String narrative,
            Instant now
    ) {
        return new MarketingStrategy(
                UUID.randomUUID(), organizationId, companyId, goalId, workflowId, StrategyStatus.DRAFT,
                positioning, icpSummary, channelsJson, pillarsJson, kpisJson, narrative, now, now, 0
        );
    }

    public static MarketingStrategy reconstitute(
            UUID id,
            OrganizationId organizationId,
            UUID companyId,
            UUID goalId,
            UUID workflowId,
            StrategyStatus status,
            String positioning,
            String icpSummary,
            String channelsJson,
            String pillarsJson,
            String kpisJson,
            String narrative,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new MarketingStrategy(
                id, organizationId, companyId, goalId, workflowId, status, positioning, icpSummary,
                channelsJson, pillarsJson, kpisJson, narrative, createdAt, updatedAt, version
        );
    }

    public void approve(Instant now) {
        if (status != StrategyStatus.DRAFT) {
            throw new DomainException("Only a draft strategy can be approved");
        }
        this.status = StrategyStatus.APPROVED;
        this.updatedAt = now;
    }

    public void reject(Instant now) {
        if (status != StrategyStatus.DRAFT) {
            throw new DomainException("Only a draft strategy can be rejected");
        }
        this.status = StrategyStatus.REJECTED;
        this.updatedAt = now;
    }

    public void supersede(Instant now) {
        if (status == StrategyStatus.APPROVED || status == StrategyStatus.DRAFT) {
            this.status = StrategyStatus.SUPERSEDED;
            this.updatedAt = now;
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID companyId() {
        return companyId;
    }

    public UUID goalId() {
        return goalId;
    }

    public UUID workflowId() {
        return workflowId;
    }

    public StrategyStatus status() {
        return status;
    }

    public String positioning() {
        return positioning;
    }

    public String icpSummary() {
        return icpSummary;
    }

    public String channelsJson() {
        return channelsJson;
    }

    public String pillarsJson() {
        return pillarsJson;
    }

    public String kpisJson() {
        return kpisJson;
    }

    public String narrative() {
        return narrative;
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
