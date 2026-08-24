package com.mavora.agents.infrastructure.persistence;

import com.mavora.agents.domain.AgentRun;
import com.mavora.agents.domain.AgentRunRepository;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaAgentRunRepository implements AgentRunRepository {

    private final AgentRunJpaRepository jpaRepository;

    public JpaAgentRunRepository(AgentRunJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AgentRun save(AgentRun run) {
        AgentRunEntity entity = jpaRepository.findById(run.id()).orElseGet(AgentRunEntity::new);
        boolean creating = entity.getId() == null;
        copy(run, entity);
        entity.markNew(creating);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<AgentRun> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByStartedAtDesc(organizationId.value()).stream()
                .map(JpaAgentRunRepository::toDomain)
                .toList();
    }

    @Override
    public List<AgentRun> findByWorkflow(UUID workflowId) {
        return jpaRepository.findByWorkflowIdOrderByStartedAtAsc(workflowId).stream()
                .map(JpaAgentRunRepository::toDomain)
                .toList();
    }

    @Override
    public long sumCostCentsSince(OrganizationId organizationId, Instant fromInclusive) {
        return jpaRepository.sumCostCentsSince(organizationId.value(), fromInclusive);
    }

    static AgentRun toDomain(AgentRunEntity entity) {
        return AgentRun.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getWorkflowId(),
                entity.getAgentType(),
                entity.getStatus(),
                entity.getModel(),
                entity.getPromptTokens(),
                entity.getCompletionTokens(),
                entity.getCostCents(),
                entity.getInputJson(),
                entity.getOutputJson(),
                entity.getErrorMessage(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                entity.getVersion()
        );
    }

    static void copy(AgentRun run, AgentRunEntity entity) {
        entity.setId(run.id());
        entity.setOrganizationId(run.organizationId().value());
        entity.setWorkflowId(run.workflowId());
        entity.setAgentType(run.agentType());
        entity.setStatus(run.status());
        entity.setModel(run.model());
        entity.setPromptTokens(run.promptTokens());
        entity.setCompletionTokens(run.completionTokens());
        entity.setCostCents(run.costCents());
        entity.setInputJson(run.inputJson());
        entity.setOutputJson(run.outputJson());
        entity.setErrorMessage(run.errorMessage());
        entity.setCreatedAt(run.startedAt());
        entity.setStartedAt(run.startedAt());
        entity.setFinishedAt(run.finishedAt());
        entity.setVersion(run.version());
    }
}
