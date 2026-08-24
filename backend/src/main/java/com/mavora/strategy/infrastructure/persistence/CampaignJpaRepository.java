package com.mavora.strategy.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignJpaRepository extends JpaRepository<CampaignEntity, UUID> {

    List<CampaignEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
