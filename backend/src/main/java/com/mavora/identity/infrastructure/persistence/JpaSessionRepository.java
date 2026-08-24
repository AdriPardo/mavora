package com.mavora.identity.infrastructure.persistence;

import com.mavora.identity.domain.Session;
import com.mavora.identity.domain.SessionRepository;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaSessionRepository implements SessionRepository {

    private final SessionJpaRepository jpaRepository;

    public JpaSessionRepository(SessionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Session save(Session session) {
        SessionEntity entity = toEntity(session);
        entity.markNew(!jpaRepository.existsById(session.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Session> findActiveByTokenHash(String tokenHash, Instant now) {
        return jpaRepository.findActiveByTokenHash(tokenHash, now).map(JpaSessionRepository::toDomain);
    }

    static Session toDomain(SessionEntity entity) {
        return Session.reconstitute(
                entity.getId(),
                new UserId(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt(),
                entity.getIp(),
                entity.getUserAgent()
        );
    }

    static SessionEntity toEntity(Session session) {
        SessionEntity entity = new SessionEntity();
        entity.setId(session.id());
        entity.setUserId(session.userId().value());
        entity.setTokenHash(session.tokenHash());
        entity.setExpiresAt(session.expiresAt());
        entity.setCreatedAt(session.createdAt());
        entity.setRevokedAt(session.revokedAt().orElse(null));
        entity.setIp(session.ip().orElse(null));
        entity.setUserAgent(session.userAgent().orElse(null));
        return entity;
    }
}
