package com.mavora.content.infrastructure.persistence;
import com.mavora.content.domain.PieceStatus;
import java.util.List; import java.util.Optional; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ContentPieceJpaRepository extends JpaRepository<ContentPieceEntity, UUID> {
    Optional<ContentPieceEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
    List<ContentPieceEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
    Optional<ContentPieceEntity> findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(UUID organizationId, PieceStatus status);
}
