package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstagramSlotRepository {

    InstagramSlot save(InstagramSlot slot);

    Optional<InstagramSlot> findById(UUID id, OrganizationId organizationId);

    List<InstagramSlot> findByOrganization(OrganizationId organizationId);

    List<InstagramSlot> findScheduled(OrganizationId organizationId);

    Optional<InstagramSlot> lockNextDue(Instant now);
}
