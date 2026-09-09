package com.mavora.identity.application;

import com.mavora.shared.domain.UserId;
import java.time.Instant;

public record AuthenticationResult(UserId userId, String rawSessionToken, Instant expiresAt) {
}
