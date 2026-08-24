package com.mavora.analytics.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.HandlerResult;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.application.WorkflowHandler;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.analytics.domain.Insight;
import com.mavora.analytics.domain.InsightRepository;
import com.mavora.analytics.domain.Learning;
import com.mavora.analytics.domain.LearningRepository;
import com.mavora.analytics.domain.MetricSnapshot;
import com.mavora.analytics.domain.MetricSnapshotRepository;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.shared.domain.DomainException;
import java.time.Clock;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsCycleHandler implements WorkflowHandler {

    private final MetricSnapshotRepository snapshotRepository;
    private final InsightRepository insightRepository;
    private final LearningRepository learningRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AnalyticsCycleHandler(
            MetricSnapshotRepository snapshotRepository,
            InsightRepository insightRepository,
            LearningRepository learningRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.snapshotRepository = snapshotRepository;
        this.insightRepository = insightRepository;
        this.learningRepository = learningRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.ANALYTICS_CYCLE;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        var snapshots = snapshotRepository.findByOrganization(execution.organizationId());
        if (snapshots.isEmpty()) {
            throw new DomainException("Record at least one metric snapshot before running analytics");
        }
        String snapshotText = snapshots.stream()
                .map(item -> item.metric() + "=" + item.value() + " @" + item.capturedAt())
                .collect(Collectors.joining("\n"));
        String system = "Eres analyst de Mavora. JSON: insightTitle, insightBody, learningTitle, learningBody. No inventes cifras que no estén en los snapshots.";
        LlmCompletion completion = llm.complete(
                execution.organizationId(), AgentType.ANALYST, system, "Snapshots:\n" + snapshotText
        );
        try {
            JsonNode node = objectMapper.readTree(completion.content());
            Instant now = clock.instant();
            Insight insight = insightRepository.save(Insight.create(
                    execution.organizationId(),
                    node.path("insightTitle").asText(),
                    node.path("insightBody").asText(),
                    now
            ));
            Learning learning = learningRepository.save(Learning.create(
                    execution.organizationId(),
                    insight.id(),
                    node.path("learningTitle").asText(),
                    node.path("learningBody").asText(),
                    now
            ));
            knowledgeItemRepository.save(KnowledgeItem.create(
                    execution.organizationId(),
                    KnowledgeKind.LEARNING,
                    learning.title(),
                    learning.body(),
                    "analytics",
                    75,
                    now
            ));
            knowledgeItemRepository.save(KnowledgeItem.create(
                    execution.organizationId(),
                    KnowledgeKind.INSIGHT,
                    insight.title(),
                    insight.body(),
                    "analytics",
                    70,
                    now
            ));
            return HandlerResult.of(completion.content(), completion);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Analyst returned invalid output");
        }
    }
}
