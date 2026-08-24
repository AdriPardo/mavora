package com.mavora.agents.application;

import com.mavora.agents.domain.AgentType;
import com.mavora.shared.domain.OrganizationId;

public record LlmRequest(
        OrganizationId organizationId,
        AgentType agentType,
        String model,
        String systemPrompt,
        String userPrompt
) {
}
