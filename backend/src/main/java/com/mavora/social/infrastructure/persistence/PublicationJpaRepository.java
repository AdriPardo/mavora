package com.mavora.social.infrastructure.persistence;
import java.util.List; import java.util.Optional; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PublicationJpaRepository extends JpaRepository<PublicationEntity, UUID> {
    Optional<PublicationEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<PublicationEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
