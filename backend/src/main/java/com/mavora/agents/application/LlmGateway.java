package com.mavora.agents.application;

import com.mavora.agents.domain.AgentRunRepository;
import com.mavora.agents.domain.AgentType;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmGateway {

    private final LlmClient llmClient;
    private final AgentRunRepository agentRunRepository;
    private final Clock clock;
    private final long monthlyBudgetCents;
    private final String defaultModel;

    public LlmGateway(
            LlmClient llmClient,
            AgentRunRepository agentRunRepository,
            Clock clock,
            @Value("${mavora.llm.monthly-budget-cents:2000}") long monthlyBudgetCents,
            @Value("${mavora.llm.model-cmo:mavora-fake}") String defaultModel
    ) {
        this.llmClient = llmClient;
        this.agentRunRepository = agentRunRepository;
        this.clock = clock;
        this.monthlyBudgetCents = monthlyBudgetCents;
        this.defaultModel = defaultModel;
    }

    public LlmCompletion complete(OrganizationId organizationId, AgentType agentType, String systemPrompt, String userPrompt) {
        Instant monthStart = YearMonth.from(clock.instant().atZone(ZoneOffset.UTC))
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        long spent = agentRunRepository.sumCostCentsSince(organizationId, monthStart);
        if (spent >= monthlyBudgetCents) {
            throw new DomainException(
                    DomainException.ErrorType.RULE,
                    "LLM budget exceeded",
                    "Monthly LLM budget for this organization has been reached"
            );
        }
        return llmClient.complete(new LlmRequest(
                organizationId, agentType, defaultModel, systemPrompt, userPrompt
        ));
    }

    public long monthlyBudgetCents() {
        return monthlyBudgetCents;
    }

    public long spentThisMonth(OrganizationId organizationId) {
        Instant monthStart = YearMonth.from(clock.instant().atZone(ZoneOffset.UTC))
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
        return agentRunRepository.sumCostCentsSince(organizationId, monthStart);
    }

    public String defaultModel() {
        return defaultModel;
    }
}
