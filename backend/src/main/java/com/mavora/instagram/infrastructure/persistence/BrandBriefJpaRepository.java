package com.mavora.instagram.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandBriefJpaRepository extends JpaRepository<BrandBriefEntity, UUID> {

    Optional<BrandBriefEntity> findByOrganizationId(UUID organizationId);
}
