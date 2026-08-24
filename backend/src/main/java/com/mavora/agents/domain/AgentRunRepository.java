package com.mavora.agents.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AgentRunRepository {

    AgentRun save(AgentRun run);

    List<AgentRun> findByOrganization(OrganizationId organizationId);

    List<AgentRun> findByWorkflow(UUID workflowId);

    long sumCostCentsSince(OrganizationId organizationId, Instant fromInclusive);
}
