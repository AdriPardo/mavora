package com.mavora.social.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.HandlerResult;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmGateway;
import com.mavora.agents.application.WorkflowHandler;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.shared.domain.DomainException;
import com.mavora.social.domain.Publication;
import com.mavora.social.domain.PublicationRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SocialPlanHandler implements WorkflowHandler {

    private final ContentPieceRepository pieceRepository;
    private final PublicationRepository publicationRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SocialPlanHandler(
            ContentPieceRepository pieceRepository,
            PublicationRepository publicationRepository,
            ObjectMapper objectMapper,
            Clock clock
    ) {
        this.pieceRepository = pieceRepository;
        this.publicationRepository = publicationRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(WorkflowType type) {
        return type == WorkflowType.SOCIAL_PLAN;
    }

    @Override
    public HandlerResult execute(WorkflowExecution execution, LlmGateway llm) {
        ContentPiece piece = pieceRepository.findLatestApproved(execution.organizationId())
                .orElseThrow(() -> new DomainException("Approve a content piece before planning social posts"));
        String system = "Eres social lead de Mavora. JSON: publications[{channel,copy}]. No publiques: solo borradores.";
        String user = "Approved piece: " + piece.title() + "\n" + piece.body();
        LlmCompletion completion = llm.complete(execution.organizationId(), AgentType.SOCIAL, system, user);
        try {
            JsonNode node = objectMapper.readTree(completion.content());
            Instant now = clock.instant();
            for (JsonNode publication : node.path("publications")) {
                publicationRepository.save(Publication.draft(
                        execution.organizationId(),
                        piece.id(),
                        publication.path("channel").asText(),
                        publication.path("copy").asText(),
                        now
                ));
            }
            return HandlerResult.of(completion.content(), completion);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Social agent returned invalid output");
        }
    }
}
