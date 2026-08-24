package com.mavora.agents.infrastructure.persistence;

import com.mavora.agents.domain.WorkflowStatus;
import com.mavora.agents.domain.WorkflowType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface WorkflowExecutionJpaRepository extends JpaRepository<WorkflowExecutionEntity, UUID> {

    Optional<WorkflowExecutionEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);

    boolean existsByOrganizationIdAndTypeAndStatusIn(
            UUID organizationId,
            WorkflowType type,
            Collection<WorkflowStatus> statuses
    );

    List<WorkflowExecutionEntity> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId, Pageable pageable);

    Optional<WorkflowExecutionEntity> findFirstByOrganizationIdAndTypeOrderByCreatedAtDesc(
            UUID organizationId,
            WorkflowType type
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
    @Query("select e from WorkflowExecutionEntity e where e.status = com.mavora.agents.domain.WorkflowStatus.QUEUED order by e.createdAt asc")
    List<WorkflowExecutionEntity> lockQueued(Pageable pageable);
}
