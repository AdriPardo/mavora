package com.mavora.company.domain;

import com.mavora.shared.domain.Money;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class MarketingGoal {

    private final UUID id;
    private final OrganizationId organizationId;
    private final String metric;
    private final long targetValue;
    private final LocalDate deadline;
    private final Money budget;
    private final String market;
    private GoalStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private MarketingGoal(
            UUID id,
            OrganizationId organizationId,
            String metric,
            long targetValue,
            LocalDate deadline,
            Money budget,
            String market,
            GoalStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.metric = requireMetric(metric);
        if (targetValue <= 0) {
            throw new IllegalArgumentException("target must be greater than zero");
        }
        this.targetValue = targetValue;
        this.deadline = Objects.requireNonNull(deadline, "deadline is required");
        this.budget = Objects.requireNonNull(budget);
        this.market = requireMarket(market);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static MarketingGoal create(
            OrganizationId organizationId,
            String metric,
            long targetValue,
            LocalDate deadline,
            Money budget,
            String market,
            Instant now
    ) {
        return new MarketingGoal(
                UUID.randomUUID(),
                organizationId,
                metric,
                targetValue,
                deadline,
                budget,
                market,
                GoalStatus.ACTIVE,
                now,
                now,
                0
        );
    }

    public static MarketingGoal reconstitute(
            UUID id,
            OrganizationId organizationId,
            String metric,
            long targetValue,
            LocalDate deadline,
            Money budget,
            String market,
            GoalStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new MarketingGoal(
                id, organizationId, metric, targetValue, deadline, budget, market, status, createdAt, updatedAt, version
        );
    }

    public void archive(Instant now) {
        this.status = GoalStatus.ARCHIVED;
        this.updatedAt = now;
    }

    private static String requireMetric(String metric) {
        if (metric == null) {
            throw new IllegalArgumentException("metric is required");
        }
        String trimmed = metric.trim();
        if (trimmed.length() < 2 || trimmed.length() > 80) {
            throw new IllegalArgumentException("metric must be between 2 and 80 characters");
        }
        return trimmed;
    }

    private static String requireMarket(String market) {
        if (market == null) {
            throw new IllegalArgumentException("market is required");
        }
        String trimmed = market.trim();
        if (trimmed.length() < 2 || trimmed.length() > 200) {
            throw new IllegalArgumentException("market must be between 2 and 200 characters");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String metric() {
        return metric;
    }

    public long targetValue() {
        return targetValue;
    }

    public LocalDate deadline() {
        return deadline;
    }

    public Money budget() {
        return budget;
    }

    public String market() {
        return market;
    }

    public GoalStatus status() {
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
