package com.mavora.identity.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, UUID> {

    @Query("""
            select s from SessionEntity s
            where s.tokenHash = :hash
              and s.revokedAt is null
              and s.expiresAt > :now
            """)
    Optional<SessionEntity> findActiveByTokenHash(@Param("hash") String hash, @Param("now") Instant now);
}
