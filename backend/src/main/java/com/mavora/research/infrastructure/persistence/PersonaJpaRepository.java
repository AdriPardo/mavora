package com.mavora.research.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PersonaJpaRepository extends JpaRepository<PersonaEntity, UUID> {
    List<PersonaEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
