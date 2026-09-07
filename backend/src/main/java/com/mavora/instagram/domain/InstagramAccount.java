package com.mavora.instagram.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class InstagramAccount {

    private final UUID id;
    private final OrganizationId organizationId;
    private InstagramProvider provider;
    private String igUserId;
    private String username;
    private String pageId;
    private String tokenCiphertext;
    private Instant tokenExpiresAt;
    private boolean autonomyEnabled;
    private Instant connectedAt;
    private Instant disconnectedAt;
    private final Instant createdAt;
    private long version;

    private InstagramAccount(
            UUID id,
            OrganizationId organizationId,
            InstagramProvider provider,
            String igUserId,
            String username,
            String pageId,
            String tokenCiphertext,
            Instant tokenExpiresAt,
            boolean autonomyEnabled,
            Instant connectedAt,
            Instant disconnectedAt,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.provider = Objects.requireNonNull(provider);
        this.igUserId = requireText(igUserId, "igUserId", 1, 64);
        this.username = requireUsername(username);
        this.pageId = blankToNull(pageId);
        this.tokenCiphertext = tokenCiphertext;
        this.tokenExpiresAt = tokenExpiresAt;
        this.autonomyEnabled = autonomyEnabled;
        this.connectedAt = Objects.requireNonNull(connectedAt);
        this.disconnectedAt = disconnectedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static InstagramAccount connect(
            OrganizationId organizationId,
            InstagramProvider provider,
            String igUserId,
            String username,
            String pageId,
            String tokenCiphertext,
            Instant tokenExpiresAt,
            Instant now
    ) {
        return new InstagramAccount(
                UUID.randomUUID(),
                organizationId,
                provider,
                igUserId,
                username,
                pageId,
                requireText(tokenCiphertext, "tokenCiphertext", 1, 16_000),
                tokenExpiresAt,
                true,
                now,
                null,
                now,
                0
        );
    }

    public static InstagramAccount reconstitute(
            UUID id,
            OrganizationId organizationId,
            InstagramProvider provider,
            String igUserId,
            String username,
            String pageId,
            String tokenCiphertext,
            Instant tokenExpiresAt,
            boolean autonomyEnabled,
            Instant connectedAt,
            Instant disconnectedAt,
            Instant createdAt,
            long version
    ) {
        return new InstagramAccount(
                id, organizationId, provider, igUserId, username, pageId, tokenCiphertext,
                tokenExpiresAt, autonomyEnabled, connectedAt, disconnectedAt, createdAt, version
        );
    }

    public void reconnect(
            InstagramProvider provider,
            String igUserId,
            String username,
            String pageId,
            String tokenCiphertext,
            Instant tokenExpiresAt,
            Instant now
    ) {
        this.provider = Objects.requireNonNull(provider);
        this.igUserId = requireText(igUserId, "igUserId", 1, 64);
        this.username = requireUsername(username);
        this.pageId = blankToNull(pageId);
        this.tokenCiphertext = requireText(tokenCiphertext, "tokenCiphertext", 1, 16_000);
        this.tokenExpiresAt = tokenExpiresAt;
        this.connectedAt = now;
        this.disconnectedAt = null;
        this.autonomyEnabled = true;
    }

    public void setAutonomy(boolean enabled) {
        if (!isConnected()) {
            throw new DomainException("Connect Instagram before enabling autonomy");
        }
        this.autonomyEnabled = enabled;
    }

    public void disconnect(Instant now) {
        this.disconnectedAt = now;
        this.tokenCiphertext = null;
        this.tokenExpiresAt = null;
        this.autonomyEnabled = false;
    }

    public boolean isConnected() {
        return disconnectedAt == null && tokenCiphertext != null && !tokenCiphertext.isBlank();
    }

    public boolean canAutoPublish() {
        return isConnected() && autonomyEnabled;
    }

    private static String requireUsername(String username) {
        String trimmed = requireText(username, "username", 1, 120);
        if (trimmed.startsWith("@")) {
            trimmed = trimmed.substring(1);
        }
        if (!trimmed.matches("[A-Za-z0-9._]{1,30}")) {
            throw new IllegalArgumentException("username is not a valid Instagram handle");
        }
        return trimmed;
    }

    private static String requireText(String value, String field, int min, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + " characters");
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

    public InstagramProvider provider() {
        return provider;
    }

    public String igUserId() {
        return igUserId;
    }

    public String username() {
        return username;
    }

    public String pageId() {
        return pageId;
    }

    public String tokenCiphertext() {
        return tokenCiphertext;
    }

    public Instant tokenExpiresAt() {
        return tokenExpiresAt;
    }

    public boolean autonomyEnabled() {
        return autonomyEnabled;
    }

    public Instant connectedAt() {
        return connectedAt;
    }

    public Instant disconnectedAt() {
        return disconnectedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
