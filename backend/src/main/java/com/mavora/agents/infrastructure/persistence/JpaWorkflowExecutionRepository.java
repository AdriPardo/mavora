package com.mavora.agents.infrastructure.persistence;

import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowExecutionRepository;
import com.mavora.agents.domain.WorkflowStatus;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaWorkflowExecutionRepository implements WorkflowExecutionRepository {

    private final WorkflowExecutionJpaRepository jpaRepository;

    public JpaWorkflowExecutionRepository(WorkflowExecutionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkflowExecution save(WorkflowExecution execution) {
        WorkflowExecutionEntity entity = jpaRepository.findById(execution.id()).orElseGet(WorkflowExecutionEntity::new);
        boolean creating = entity.getId() == null;
        copy(execution, entity);
        entity.markNew(creating);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<WorkflowExecution> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value())
                .map(JpaWorkflowExecutionRepository::toDomain);
    }

    @Override
    public Optional<WorkflowExecution> lockNextQueued() {
        return jpaRepository.lockQueued(PageRequest.of(0, 1)).stream().findFirst().map(entity -> {
            entity.markNew(false);
            return toDomain(entity);
        });
    }

    @Override
    public boolean hasActive(OrganizationId organizationId, WorkflowType type) {
        return jpaRepository.existsByOrganizationIdAndTypeAndStatusIn(
                organizationId.value(),
                type,
                Set.of(WorkflowStatus.QUEUED, WorkflowStatus.RUNNING)
        );
    }

    @Override
    public List<WorkflowExecution> findRecent(OrganizationId organizationId, int limit) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(
                        organizationId.value(), PageRequest.of(0, limit)
                ).stream()
                .map(JpaWorkflowExecutionRepository::toDomain)
                .toList();
    }

    @Override
    public Optional<WorkflowExecution> findLatest(OrganizationId organizationId, WorkflowType type) {
        return jpaRepository.findFirstByOrganizationIdAndTypeOrderByCreatedAtDesc(organizationId.value(), type)
                .map(JpaWorkflowExecutionRepository::toDomain);
    }

    static WorkflowExecution toDomain(WorkflowExecutionEntity entity) {
        return WorkflowExecution.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getType(),
                entity.getStatus(),
                entity.getInputJson(),
                entity.getOutputJson(),
                entity.getErrorMessage(),
                new UserId(entity.getCreatedBy()),
                entity.getCreatedAt(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getVersion()
        );
    }

    static void copy(WorkflowExecution execution, WorkflowExecutionEntity entity) {
        entity.setId(execution.id());
        entity.setOrganizationId(execution.organizationId().value());
        entity.setType(execution.type());
        entity.setStatus(execution.status());
        entity.setInputJson(execution.inputJson());
        entity.setOutputJson(execution.outputJson());
        entity.setErrorMessage(execution.errorMessage());
        entity.setCreatedBy(execution.createdBy().value());
        entity.setCreatedAt(execution.createdAt());
        entity.setStartedAt(execution.startedAt());
        entity.setFinishedAt(execution.finishedAt());
        entity.setVersion(execution.version());
    }
}
