package com.mavora.identity.domain;

import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;

public final class User {

    private final UserId id;
    private final Email email;
    private final String passwordHash;
    private final UserStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    private User(
            UserId id,
            Email email,
            String passwordHash,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.version = version;
        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }
    }

    public static User register(UserId id, Email email, String passwordHash, Instant now) {
        return new User(id, email, passwordHash, UserStatus.ACTIVE, now, now, 0);
    }

    public static User reconstitute(
            UserId id,
            Email email,
            String passwordHash,
            UserStatus status,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        return new User(id, email, passwordHash, status, createdAt, updatedAt, version);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public UserStatus status() {
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
