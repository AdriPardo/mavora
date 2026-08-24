package com.mavora.strategy.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketingStrategyRepository {

    MarketingStrategy save(MarketingStrategy strategy);

    Optional<MarketingStrategy> findById(UUID id, OrganizationId organizationId);

    Optional<MarketingStrategy> findLatest(OrganizationId organizationId);

    Optional<MarketingStrategy> findApproved(OrganizationId organizationId);

    List<MarketingStrategy> findByOrganization(OrganizationId organizationId);
}
