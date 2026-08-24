package com.mavora.analytics.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MetricSnapshot {
    private final UUID id;
    private final OrganizationId organizationId;
    private final String metric;
    private final long value;
    private final Instant capturedAt;
    private final String source;
    private final Instant createdAt;
    private final long version;

    private MetricSnapshot(UUID id, OrganizationId organizationId, String metric, long value, Instant capturedAt,
                           String source, Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.metric = Objects.requireNonNull(metric);
        this.value = value;
        this.capturedAt = Objects.requireNonNull(capturedAt);
        this.source = Objects.requireNonNull(source);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
        if (metric.isBlank() || metric.length() > 80) {
            throw new IllegalArgumentException("metric is invalid");
        }
    }

    public static MetricSnapshot create(OrganizationId organizationId, String metric, long value,
                                        Instant capturedAt, String source, Instant now) {
        return new MetricSnapshot(UUID.randomUUID(), organizationId, metric.trim(), value, capturedAt, source, now, 0);
    }

    public static MetricSnapshot reconstitute(UUID id, OrganizationId organizationId, String metric, long value,
                                              Instant capturedAt, String source, Instant createdAt, long version) {
        return new MetricSnapshot(id, organizationId, metric, value, capturedAt, source, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public String metric() { return metric; }
    public long value() { return value; }
    public Instant capturedAt() { return capturedAt; }
    public String source() { return source; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
