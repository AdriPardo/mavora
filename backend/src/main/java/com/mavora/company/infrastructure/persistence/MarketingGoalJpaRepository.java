package com.mavora.company.infrastructure.persistence;

import com.mavora.company.domain.GoalStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketingGoalJpaRepository extends JpaRepository<MarketingGoalEntity, UUID> {

    Optional<MarketingGoalEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    Optional<MarketingGoalEntity> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
            UUID organizationId,
            GoalStatus status
    );

    List<MarketingGoalEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
