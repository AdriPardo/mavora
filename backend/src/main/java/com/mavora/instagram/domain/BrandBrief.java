package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class BrandBrief {

    private final UUID id;
    private final OrganizationId organizationId;
    private String voice;
    private String offer;
    private String cta;
    private String audience;
    private String extraNotes;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private BrandBrief(
            UUID id,
            OrganizationId organizationId,
            String voice,
            String offer,
            String cta,
            String audience,
            String extraNotes,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.voice = clamp(voice, 500);
        this.offer = clamp(offer, 2000);
        this.cta = clamp(cta, 300);
        this.audience = clamp(audience, 500);
        this.extraNotes = clamp(extraNotes, 4000);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static BrandBrief create(
            OrganizationId organizationId,
            String voice,
            String offer,
            String cta,
            String audience,
            String extraNotes,
            Instant now
    ) {
        return new BrandBrief(UUID.randomUUID(), organizationId, voice, offer, cta, audience, extraNotes, now, now, 0);
    }

    public static BrandBrief reconstitute(
            UUID id,
            OrganizationId organizationId,
            String voice,
            String offer,
            String cta,
            String audience,
            String extraNotes,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new BrandBrief(
                id, organizationId, voice, offer, cta, audience, extraNotes, createdAt, updatedAt, version
        );
    }

    public void update(String voice, String offer, String cta, String audience, String extraNotes, Instant now) {
        this.voice = clamp(voice, 500);
        this.offer = clamp(offer, 2000);
        this.cta = clamp(cta, 300);
        this.audience = clamp(audience, 500);
        this.extraNotes = clamp(extraNotes, 4000);
        this.updatedAt = now;
    }

    private static String clamp(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException("text is too long");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String voice() {
        return voice;
    }

    public String offer() {
        return offer;
    }

    public String cta() {
        return cta;
    }

    public String audience() {
        return audience;
    }

    public String extraNotes() {
        return extraNotes;
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
