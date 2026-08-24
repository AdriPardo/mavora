package com.mavora.strategy.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.HandlerResult;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.application.WorkflowHandler;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.approvals.domain.ApprovalRequest;
import com.mavora.approvals.domain.ApprovalRequestRepository;
import com.mavora.approvals.domain.ApprovalType;
import com.mavora.company.domain.Company;
import com.mavora.company.domain.CompanyRepository;
import com.mavora.company.domain.MarketingGoal;
import com.mavora.company.domain.MarketingGoalRepository;
import com.mavora.company.domain.Product;
import com.mavora.company.domain.ProductRepository;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.shared.domain.DomainException;
import com.mavora.strategy.domain.MarketingStrategy;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CmoStrategyHandler implements WorkflowHandler {

    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final MarketingGoalRepository goalRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final MarketingStrategyRepository strategyRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public CmoStrategyHandler(
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            MarketingGoalRepository goalRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            MarketingStrategyRepository strategyRepository,
            ApprovalRequestRepository approvalRequestRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.goalRepository = goalRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.strategyRepository = strategyRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.CMO_STRATEGY;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        Company company = companyRepository.findByOrganization(execution.organizationId())
                .orElseThrow(() -> new DomainException("Connect the company before asking the CMO for a strategy"));
        MarketingGoal goal = goalRepository.findActive(execution.organizationId())
                .orElseThrow(() -> new DomainException("Define a marketing goal before asking the CMO for a strategy"));
        String products = productRepository.findByCompany(company.id()).stream()
                .map(Product::name)
                .collect(Collectors.joining(", "));
        String knowledge = knowledgeItemRepository.findRecent(execution.organizationId(), 8).stream()
                .map(item -> item.kind() + ": " + item.title() + " — " + item.body())
                .collect(Collectors.joining("\n"));
        String system = """
                Eres el CMO de Mavora. Devuelves SOLO JSON con: positioning, icpSummary, channels (array de strings), \
                pillars (array de strings), kpis (array de {name,target}), narrative. No eres un chatbot: propones un \
                plan accionable para un founder en España. No inventes métricas de resultados que no existen.
                """;
        String user = """
                Company: %s
                Market: %s
                Website: %s
                Products: %s
                Metric: %s
                Target: %s
                Deadline: %s
                BudgetCents: %s %s
                Knowledge:
                %s
                """.formatted(
                company.name(),
                company.market() == null ? "" : company.market(),
                company.websiteUrl() == null ? "" : company.websiteUrl(),
                products,
                goal.metric(),
                goal.targetValue(),
                goal.deadline(),
                goal.budget().amountCents(),
                goal.budget().currency().getCurrencyCode(),
                knowledge
        );
        LlmCompletion completion = llm.complete(execution.organizationId(), AgentType.CMO, system, user);
        try {
            JsonNode node = objectMapper.readTree(completion.content());
            Instant now = clock.instant();
            MarketingStrategy strategy = strategyRepository.save(MarketingStrategy.draft(
                    execution.organizationId(),
                    company.id(),
                    goal.id(),
                    execution.id(),
                    text(node, "positioning"),
                    text(node, "icpSummary"),
                    objectMapper.writeValueAsString(node.path("channels")),
                    objectMapper.writeValueAsString(node.path("pillars")),
                    objectMapper.writeValueAsString(node.path("kpis")),
                    text(node, "narrative"),
                    now
            ));
            approvalRequestRepository.save(ApprovalRequest.pending(
                    execution.organizationId(),
                    ApprovalType.STRATEGY,
                    "MARKETING_STRATEGY",
                    strategy.id(),
                    "Estrategia CMO para " + company.name(),
                    completion.content(),
                    now
            ));
            return HandlerResult.of(objectMapper.createObjectNode()
                    .put("strategyId", strategy.id().toString())
                    .toString(), completion);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("CMO returned an invalid strategy");
        }
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText("");
        if (value.isBlank()) {
            throw new DomainException("CMO returned an invalid strategy");
        }
        return value;
    }
}
