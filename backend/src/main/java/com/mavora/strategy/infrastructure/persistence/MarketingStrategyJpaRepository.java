package com.mavora.strategy.infrastructure.persistence;

import com.mavora.strategy.domain.StrategyStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketingStrategyJpaRepository extends JpaRepository<MarketingStrategyEntity, UUID> {

    Optional<MarketingStrategyEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<MarketingStrategyEntity> findFirstByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    Optional<MarketingStrategyEntity> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
            UUID organizationId,
            StrategyStatus status
    );

    List<MarketingStrategyEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
