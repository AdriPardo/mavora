package com.mavora.instagram.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetJpaRepository extends JpaRepository<MediaAssetEntity, UUID> {

    List<MediaAssetEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    Optional<MediaAssetEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
