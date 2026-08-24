package com.mavora.knowledge.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeItemJpaRepository extends JpaRepository<KnowledgeItemEntity, UUID> {

    List<KnowledgeItemEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    List<KnowledgeItemEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);
}
