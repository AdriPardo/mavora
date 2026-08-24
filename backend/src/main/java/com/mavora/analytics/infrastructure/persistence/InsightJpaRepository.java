package com.mavora.analytics.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InsightJpaRepository extends JpaRepository<InsightEntity, UUID> {
    List<InsightEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
