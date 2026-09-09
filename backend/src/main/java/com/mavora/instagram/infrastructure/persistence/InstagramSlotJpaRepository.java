package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.InstagramSlotStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface InstagramSlotJpaRepository extends JpaRepository<InstagramSlotEntity, UUID> {

    List<InstagramSlotEntity> findByOrganizationIdOrderByScheduledAtAsc(UUID organizationId);

    List<InstagramSlotEntity> findByOrganizationIdAndStatusOrderByScheduledAtAsc(
            UUID organizationId, InstagramSlotStatus status
    );

    Optional<InstagramSlotEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("""
            select e from InstagramSlotEntity e
            where e.status = com.mavora.instagram.domain.InstagramSlotStatus.SCHEDULED
              and e.scheduledAt <= :now
              and exists (
                select 1 from InstagramAccountEntity a
                where a.organizationId = e.organizationId
                  and a.disconnectedAt is null
                  and a.tokenCiphertext is not null
                  and a.tokenCiphertext <> ''
                  and a.autonomyEnabled = true
              )
            order by e.scheduledAt asc
            """)
    List<InstagramSlotEntity> lockDue(Instant now, Pageable pageable);
}
