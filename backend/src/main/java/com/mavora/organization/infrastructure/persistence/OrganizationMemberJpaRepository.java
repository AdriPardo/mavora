package com.mavora.organization.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberJpaRepository extends JpaRepository<OrganizationMemberEntity, UUID> {

    Optional<OrganizationMemberEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    List<OrganizationMemberEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMemberEntity> findByUserId(UUID userId);
}
