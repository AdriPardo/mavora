package com.mavora.organization.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;

public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findById(OrganizationId id);

    boolean existsBySlug(String slug);
}
