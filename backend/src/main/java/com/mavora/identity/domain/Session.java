package com.mavora.identity.domain;

import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Session {

    private final UUID id;
    private final UserId userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant revokedAt;
    private final String ip;
    private final String userAgent;

    private Session(
            UUID id,
            UserId userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant revokedAt,
            String ip,
            String userAgent
    ) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.revokedAt = revokedAt;
        this.ip = ip;
        this.userAgent = userAgent;
        if (tokenHash.length() != 64) {
            throw new IllegalArgumentException("token hash must be SHA-256 hex");
        }
    }

    public static Session issue(
            UserId userId,
            String tokenHash,
            Instant expiresAt,
            Instant now,
            String ip,
            String userAgent
    ) {
        return new Session(UUID.randomUUID(), userId, tokenHash, expiresAt, now, null, ip, userAgent);
    }

    public static Session reconstitute(
            UUID id,
            UserId userId,
            String tokenHash,
            Instant expiresAt,
            Instant createdAt,
            Instant revokedAt,
            String ip,
            String userAgent
    ) {
        return new Session(id, userId, tokenHash, expiresAt, createdAt, revokedAt, ip, userAgent);
    }

    public boolean isActive(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    public void revoke(Instant now) {
        if (revokedAt == null) {
            this.revokedAt = now;
        }
    }

    public UUID id() {
        return id;
    }

    public UserId userId() {
        return userId;
    }

    public String tokenHash() {
        return tokenHash;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Optional<Instant> revokedAt() {
        return Optional.ofNullable(revokedAt);
    }

    public Optional<String> ip() {
        return Optional.ofNullable(ip);
    }

    public Optional<String> userAgent() {
        return Optional.ofNullable(userAgent);
    }
}
