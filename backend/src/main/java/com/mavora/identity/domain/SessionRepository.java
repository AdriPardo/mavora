package com.mavora.identity.domain;

import java.time.Instant;
import java.util.Optional;

public interface SessionRepository {

    Session save(Session session);

    Optional<Session> findActiveByTokenHash(String tokenHash, Instant now);
}
