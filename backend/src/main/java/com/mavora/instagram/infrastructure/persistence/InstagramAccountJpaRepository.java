package com.mavora.instagram.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstagramAccountJpaRepository extends JpaRepository<InstagramAccountEntity, UUID> {

    Optional<InstagramAccountEntity> findByOrganizationId(UUID organizationId);
}
