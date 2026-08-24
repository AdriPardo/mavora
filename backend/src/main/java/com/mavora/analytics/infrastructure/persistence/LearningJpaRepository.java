package com.mavora.analytics.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LearningJpaRepository extends JpaRepository<LearningEntity, UUID> {
    List<LearningEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
