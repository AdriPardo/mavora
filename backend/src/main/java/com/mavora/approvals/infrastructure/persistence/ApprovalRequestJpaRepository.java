package com.mavora.approvals.infrastructure.persistence;

import com.mavora.approvals.domain.ApprovalStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestJpaRepository extends JpaRepository<ApprovalRequestEntity, UUID> {

    Optional<ApprovalRequestEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<ApprovalRequestEntity> findByOrganizationIdAndStatusOrderByCreatedAtDesc(
            UUID organizationId,
            ApprovalStatus status
    );

    List<ApprovalRequestEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);

    long countByOrganizationIdAndStatus(UUID organizationId, ApprovalStatus status);
}
