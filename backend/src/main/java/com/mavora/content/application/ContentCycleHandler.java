package com.mavora.content.application;

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
import com.mavora.content.domain.ContentIdea;
import com.mavora.content.domain.ContentIdeaRepository;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.content.domain.ContentVariant;
import com.mavora.content.domain.ContentVariantRepository;
import com.mavora.shared.domain.DomainException;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ContentCycleHandler implements WorkflowHandler {

    private final MarketingStrategyRepository strategyRepository;
    private final ContentIdeaRepository ideaRepository;
    private final ContentPieceRepository pieceRepository;
    private final ContentVariantRepository variantRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ContentCycleHandler(
            MarketingStrategyRepository strategyRepository,
            ContentIdeaRepository ideaRepository,
            ContentPieceRepository pieceRepository,
            ContentVariantRepository variantRepository,
            ApprovalRequestRepository approvalRequestRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.strategyRepository = strategyRepository;
        this.ideaRepository = ideaRepository;
        this.pieceRepository = pieceRepository;
        this.variantRepository = variantRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.CONTENT_CYCLE;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        var strategy = strategyRepository.findApproved(execution.organizationId())
                .orElseThrow(() -> new DomainException("Approve a strategy before generating content"));
        String system = "Eres content lead de Mavora. JSON: ideas[{title,angle,pillar}], piece{title,body,ideaTitle,variants[{channel,body}]}.";
        String user = "Positioning: " + strategy.positioning() + "\nPillars: " + strategy.pillarsJson();
        LlmCompletion completion = llm.complete(execution.organizationId(), AgentType.CONTENT, system, user);
        try {
            JsonNode node = objectMapper.readTree(completion.content());
            Instant now = clock.instant();
            UUID firstIdeaId = null;
            for (JsonNode idea : node.path("ideas")) {
                ContentIdea saved = ideaRepository.save(ContentIdea.create(
                        execution.organizationId(),
                        idea.path("title").asText(),
                        idea.path("angle").asText(),
                        idea.path("pillar").asText(null),
                        now
                ));
                if (firstIdeaId == null) {
                    firstIdeaId = saved.id();
                }
            }
            JsonNode pieceNode = node.path("piece");
            if (firstIdeaId == null) {
                throw new DomainException("Content agent returned no ideas");
            }
            ContentPiece piece = pieceRepository.save(ContentPiece.pending(
                    execution.organizationId(),
                    firstIdeaId,
                    pieceNode.path("title").asText(),
                    pieceNode.path("body").asText(),
                    now
            ));
            for (JsonNode variant : pieceNode.path("variants")) {
                variantRepository.save(ContentVariant.create(
                        execution.organizationId(),
                        piece.id(),
                        variant.path("channel").asText(),
                        variant.path("body").asText(),
                        now
                ));
            }
            approvalRequestRepository.save(ApprovalRequest.pending(
                    execution.organizationId(),
                    ApprovalType.CONTENT_PIECE,
                    "CONTENT_PIECE",
                    piece.id(),
                    "Pieza: " + piece.title(),
                    completion.content(),
                    now
            ));
            return HandlerResult.of(objectMapper.createObjectNode()
                    .put("pieceId", piece.id().toString())
                    .toString(), completion);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Content agent returned invalid output");
        }
    }
}
