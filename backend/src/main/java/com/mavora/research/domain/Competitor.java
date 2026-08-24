package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Competitor {
    private final UUID id;
    private final OrganizationId organizationId;
    private final String name;
    private final String url;
    private final String notes;
    private final Instant createdAt;
    private final long version;

    private Competitor(UUID id, OrganizationId organizationId, String name, String url, String notes,
                       Instant createdAt, long version) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.name = Objects.requireNonNull(name);
        this.url = url;
        this.notes = Objects.requireNonNull(notes);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static Competitor create(OrganizationId organizationId, String name, String url, String notes, Instant now) {
        return new Competitor(UUID.randomUUID(), organizationId, name, url, notes, now, 0);
    }

    public static Competitor reconstitute(UUID id, OrganizationId organizationId, String name, String url,
                                          String notes, Instant createdAt, long version) {
        return new Competitor(id, organizationId, name, url, notes, createdAt, version);
    }

    public UUID id() { return id; }
    public OrganizationId organizationId() { return organizationId; }
    public String name() { return name; }
    public String url() { return url; }
    public String notes() { return notes; }
    public Instant createdAt() { return createdAt; }
    public long version() { return version; }
}
