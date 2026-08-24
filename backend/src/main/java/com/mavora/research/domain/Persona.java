package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Persona {
    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID strategyId;
    private final String name;
    private final String summary;
    private final String pains;
    private final String jobs;
    private final Instant createdAt;
    private final long version;

    private Persona(UUID id, OrganizationId organizationId, UUID strategyId, String name, String summary,
                    String pains, String jobs, Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.strategyId = Objects.requireNonNull(strategyId);
        this.name = Objects.requireNonNull(name);
        this.summary = Objects.requireNonNull(summary);
        this.pains = Objects.requireNonNull(pains);
        this.jobs = Objects.requireNonNull(jobs);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Persona create(OrganizationId organizationId, UUID strategyId, String name, String summary,
                                 String pains, String jobs, Instant now) {
        return new Persona(UUID.randomUUID(), organizationId, strategyId, name, summary, pains, jobs, now, 0);
    }

    public static Persona reconstitute(UUID id, OrganizationId organizationId, UUID strategyId, String name,
                                       String summary, String pains, String jobs, Instant createdAt, long version) {
        return new Persona(id, organizationId, strategyId, name, summary, pains, jobs, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public UUID strategyId() { return strategyId; }
    public String name() { return name; }
    public String summary() { return summary; }
    public String pains() { return pains; }
    public String jobs() { return jobs; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
