package com.mavora.content.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ContentIdeaJpaRepository extends JpaRepository<ContentIdeaEntity, UUID> {
    List<ContentIdeaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
