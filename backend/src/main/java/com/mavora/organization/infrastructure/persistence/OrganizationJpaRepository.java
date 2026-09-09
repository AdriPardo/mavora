package com.mavora.organization.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {

    boolean existsBySlug(String slug);
}
