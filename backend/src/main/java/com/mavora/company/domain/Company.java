package com.mavora.company.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Company {

    private final UUID id;
    private final OrganizationId organizationId;
    private String name;
    private String websiteUrl;
    private String description;
    private String market;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private Company(
            UUID id,
            OrganizationId organizationId,
            String name,
            String websiteUrl,
            String description,
            String market,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.name = requireName(name);
        this.websiteUrl = websiteUrl;
        this.description = requireDescription(description);
        this.market = requireMarket(market);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static Company create(
            OrganizationId organizationId,
            String name,
            String websiteUrl,
            String description,
            String market,
            Instant now
    ) {
        return new Company(
                UUID.randomUUID(),
                organizationId,
                name,
                HttpUrl.normalizeOptional(websiteUrl).orElse(null),
                description,
                market,
                now,
                now,
                0
        );
    }

    public static Company reconstitute(
            UUID id,
            OrganizationId organizationId,
            String name,
            String websiteUrl,
            String description,
            String market,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new Company(
                id, organizationId, name, websiteUrl, description, market, createdAt, updatedAt, version
        );
    }

    public void update(String name, String websiteUrl, String description, String market, Instant now) {
        this.name = requireName(name);
        this.websiteUrl = HttpUrl.normalizeOptional(websiteUrl).orElse(null);
        this.description = requireDescription(description);
        this.market = requireMarket(market);
        this.updatedAt = now;
    }

    public java.util.List<String> fillBlanks(
            String websiteUrl,
            String description,
            String market,
            Instant now
    ) {
        java.util.List<String> filled = new java.util.ArrayList<>();
        if (blank(this.websiteUrl) && notBlank(websiteUrl)) {
            this.websiteUrl = HttpUrl.normalizeOptional(websiteUrl).orElse(null);
            if (this.websiteUrl != null) {
                filled.add("company.websiteUrl");
            }
        }
        if (blank(this.description) && notBlank(description)) {
            this.description = requireDescription(description);
            filled.add("company.description");
        }
        if (blank(this.market) && notBlank(market)) {
            this.market = requireMarket(market);
            filled.add("company.market");
        }
        if (!filled.isEmpty()) {
            this.updatedAt = now;
        }
        return filled;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String requireName(String name) {
        return requireText(name, "name", 2, 120);
    }

    private static String requireDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.length() > 4000) {
            throw new IllegalArgumentException("description is too long");
        }
        return trimmed;
    }

    private static String requireMarket(String market) {
        if (market == null || market.isBlank()) {
            return null;
        }
        return requireText(market, "market", 2, 200);
    }

    private static String requireText(String value, String field, int min, int max) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + " characters");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String name() {
        return name;
    }

    public String websiteUrl() {
        return websiteUrl;
    }

    public String description() {
        return description;
    }

    public String market() {
        return market;
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
