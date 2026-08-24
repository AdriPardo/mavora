package com.mavora.agents.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkflowExecutionRepository {

    WorkflowExecution save(WorkflowExecution execution);

    Optional<WorkflowExecution> findById(UUID id, OrganizationId organizationId);

    Optional<WorkflowExecution> lockNextQueued();

    boolean hasActive(OrganizationId organizationId, WorkflowType type);

    List<WorkflowExecution> findRecent(OrganizationId organizationId, int limit);

    Optional<WorkflowExecution> findLatest(OrganizationId organizationId, WorkflowType type);
}
