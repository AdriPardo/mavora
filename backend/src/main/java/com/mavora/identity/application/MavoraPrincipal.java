package com.mavora.identity.application;

import com.mavora.shared.domain.UserId;
import java.util.Objects;

public record MavoraPrincipal(UserId userId, String email) {

    public MavoraPrincipal {
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(email, "email is required");
    }
}
