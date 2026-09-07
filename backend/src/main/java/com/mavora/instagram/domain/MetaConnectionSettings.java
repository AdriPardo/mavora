package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MetaConnectionSettings {

    private final UUID id;
    private final OrganizationId organizationId;
    private String appId;
    private String appSecretCiphertext;
    private String redirectUri;
    private String graphVersion;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private MetaConnectionSettings(
            UUID id,
            OrganizationId organizationId,
            String appId,
            String appSecretCiphertext,
            String redirectUri,
            String graphVersion,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.appId = clamp(appId, 64);
        this.appSecretCiphertext = blankToNull(appSecretCiphertext);
        this.redirectUri = clamp(redirectUri, 500);
        this.graphVersion = normalizeGraphVersion(graphVersion);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
    }

    public static MetaConnectionSettings create(
            OrganizationId organizationId,
            String appId,
            String appSecretCiphertext,
            String redirectUri,
            String graphVersion,
            Instant now
    ) {
        return new MetaConnectionSettings(
                UUID.randomUUID(),
                organizationId,
                appId,
                appSecretCiphertext,
                redirectUri,
                graphVersion,
                now,
                now,
                0
        );
    }

    public static MetaConnectionSettings reconstitute(
            UUID id,
            OrganizationId organizationId,
            String appId,
            String appSecretCiphertext,
            String redirectUri,
            String graphVersion,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new MetaConnectionSettings(
                id, organizationId, appId, appSecretCiphertext, redirectUri, graphVersion,
                createdAt, updatedAt, version
        );
    }

    public void update(String appId, String appSecretCiphertext, String redirectUri, String graphVersion, Instant now) {
        this.appId = clamp(appId, 64);
        if (appSecretCiphertext != null && !appSecretCiphertext.isBlank()) {
            this.appSecretCiphertext = appSecretCiphertext.trim();
        }
        this.redirectUri = clamp(redirectUri, 500);
        this.graphVersion = normalizeGraphVersion(graphVersion);
        this.updatedAt = now;
    }

    public void clearSecret(Instant now) {
        this.appSecretCiphertext = null;
        this.updatedAt = now;
    }

    public boolean isReady() {
        return appId != null && !appId.isBlank()
                && appSecretCiphertext != null && !appSecretCiphertext.isBlank();
    }

    private static String normalizeGraphVersion(String graphVersion) {
        String trimmed = clamp(graphVersion, 16);
        if (trimmed == null) {
            return "v21.0";
        }
        if (!trimmed.matches("v\\d{1,2}\\.\\d{1,2}")) {
            throw new IllegalArgumentException("graphVersion must look like v21.0");
        }
        return trimmed;
    }

    private static String clamp(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw new IllegalArgumentException("value exceeds " + max + " characters");
        }
        return trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public String appId() {
        return appId;
    }

    public String appSecretCiphertext() {
        return appSecretCiphertext;
    }

    public String redirectUri() {
        return redirectUri;
    }

    public String graphVersion() {
        return graphVersion;
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
