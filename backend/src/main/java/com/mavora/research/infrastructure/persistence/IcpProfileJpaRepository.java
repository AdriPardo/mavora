package com.mavora.research.infrastructure.persistence;
import java.util.Optional; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface IcpProfileJpaRepository extends JpaRepository<IcpProfileEntity, UUID> {
    Optional<IcpProfileEntity> findFirstByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
