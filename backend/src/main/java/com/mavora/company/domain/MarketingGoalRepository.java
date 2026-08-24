package com.mavora.company.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketingGoalRepository {

    MarketingGoal save(MarketingGoal goal);

    Optional<MarketingGoal> findById(UUID id, OrganizationId organizationId);

    Optional<MarketingGoal> findActive(OrganizationId organizationId);

    List<MarketingGoal> findByOrganization(OrganizationId organizationId);
}
