package com.mavora.strategy.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;

public interface CampaignRepository {

    Campaign save(Campaign campaign);

    List<Campaign> findByOrganization(OrganizationId organizationId);
}
