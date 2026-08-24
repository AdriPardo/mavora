package com.mavora.research.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CompetitorJpaRepository extends JpaRepository<CompetitorEntity, UUID> {
    List<CompetitorEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
