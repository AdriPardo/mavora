package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;

public interface BrandBriefRepository {

    BrandBrief save(BrandBrief brief);

    Optional<BrandBrief> findByOrganization(OrganizationId organizationId);
}
