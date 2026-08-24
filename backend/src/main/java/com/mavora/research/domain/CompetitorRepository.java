package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;

public interface CompetitorRepository {

    Competitor save(Competitor competitor);
    List<Competitor> findByOrganization(OrganizationId organizationId);

}
