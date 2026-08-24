package com.mavora.social.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PublicationRepository {
    Publication save(Publication publication);
    Optional<Publication> findById(UUID id, OrganizationId organizationId);
    List<Publication> findByOrganization(OrganizationId organizationId);
}
