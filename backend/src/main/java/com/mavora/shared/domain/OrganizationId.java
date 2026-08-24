package com.mavora.shared.domain;

import java.util.Objects;
import java.util.UUID;

public record OrganizationId(UUID value) {

    public OrganizationId {
        Objects.requireNonNull(value, "organization id is required");
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }

    public static OrganizationId from(String raw) {
        return new OrganizationId(UUID.fromString(raw));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
