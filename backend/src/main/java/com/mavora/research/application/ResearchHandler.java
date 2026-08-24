package com.mavora.research.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.HandlerResult;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.application.WorkflowHandler;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.research.domain.Competitor;
import com.mavora.research.domain.CompetitorRepository;
import com.mavora.research.domain.IcpProfile;
import com.mavora.research.domain.IcpProfileRepository;
import com.mavora.research.domain.Persona;
import com.mavora.research.domain.PersonaRepository;
import com.mavora.shared.domain.DomainException;
import com.mavora.strategy.domain.MarketingStrategy;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ResearchHandler implements WorkflowHandler {

    private final MarketingStrategyRepository strategyRepository;
    private final PersonaRepository personaRepository;
    private final CompetitorRepository competitorRepository;
    private final IcpProfileRepository icpProfileRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ResearchHandler(
            MarketingStrategyRepository strategyRepository,
            PersonaRepository personaRepository,
            CompetitorRepository competitorRepository,
            IcpProfileRepository icpProfileRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.strategyRepository = strategyRepository;
        this.personaRepository = personaRepository;
        this.competitorRepository = competitorRepository;
        this.icpProfileRepository = icpProfileRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.MARKET_RESEARCH;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        MarketingStrategy strategy = strategyRepository.findApproved(execution.organizationId())
                .orElseThrow(() -> new DomainException("Approve a strategy before running market research"));
        String system = "Eres researcher de Mavora. Devuelves SOLO JSON con personas[{name,summary,pains,jobs}], competitors[{name,url,notes}], icpSummary, segments (array).";
        String user = """
                Company positioning: %s
                ICP: %s
                Channels: %s
                """.formatted(strategy.positioning(), strategy.icpSummary(), strategy.channelsJson());
        LlmCompletion completion = llm.complete(execution.organizationId(), AgentType.RESEARCHER, system, user);
        try {
            JsonNode node = objectMapper.readTree(completion.content());
            Instant now = clock.instant();
            for (JsonNode persona : node.path("personas")) {
                personaRepository.save(Persona.create(
                        execution.organizationId(),
                        strategy.id(),
                        persona.path("name").asText(),
                        persona.path("summary").asText(),
                        persona.path("pains").asText(),
                        persona.path("jobs").asText(),
                        now
                ));
            }
            for (JsonNode competitor : node.path("competitors")) {
                competitorRepository.save(Competitor.create(
                        execution.organizationId(),
                        competitor.path("name").asText(),
                        competitor.path("url").asText(null),
                        competitor.path("notes").asText(),
                        now
                ));
            }
            IcpProfile icp = icpProfileRepository.save(IcpProfile.create(
                    execution.organizationId(),
                    strategy.id(),
                    node.path("icpSummary").asText(),
                    objectMapper.writeValueAsString(node.path("segments")),
                    now
            ));
            knowledgeItemRepository.save(KnowledgeItem.create(
                    execution.organizationId(),
                    KnowledgeKind.INSIGHT,
                    "ICP actualizado",
                    icp.summary(),
                    "research",
                    70,
                    now
            ));
            knowledgeItemRepository.save(KnowledgeItem.create(
                    execution.organizationId(),
                    KnowledgeKind.HYPOTHESIS,
                    "Hipótesis de canal",
                    "LinkedIn concentrará mejor la conversación con el ICP descrito.",
                    "research",
                    55,
                    now
            ));
            return HandlerResult.of(completion.content(), completion);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Researcher returned invalid output");
        }
    }
}
