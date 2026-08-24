package com.mavora.agents.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentRunJpaRepository extends JpaRepository<AgentRunEntity, UUID> {

    List<AgentRunEntity> findByOrganizationIdOrderByStartedAtDesc(UUID organizationId);

    List<AgentRunEntity> findByWorkflowIdOrderByStartedAtAsc(UUID workflowId);

    @Query("""
            select coalesce(sum(r.costCents), 0)
            from AgentRunEntity r
            where r.organizationId = :organizationId and r.startedAt >= :fromInclusive
            """)
    long sumCostCentsSince(@Param("organizationId") UUID organizationId, @Param("fromInclusive") Instant fromInclusive);
}
