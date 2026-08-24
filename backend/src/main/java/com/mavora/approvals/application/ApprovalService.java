package com.mavora.approvals.application;

import com.mavora.approvals.domain.ApprovalRequest;
import com.mavora.approvals.domain.ApprovalRequestRepository;
import com.mavora.approvals.domain.ApprovalType;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import com.mavora.shared.domain.UserId;
import com.mavora.strategy.domain.Campaign;
import com.mavora.strategy.domain.CampaignRepository;
import com.mavora.strategy.domain.MarketingStrategy;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import com.mavora.strategy.domain.StrategyStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalService {

    private final OrganizationAuthorizationService authorizationService;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final MarketingStrategyRepository strategyRepository;
    private final CampaignRepository campaignRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ContentPieceRepository contentPieceRepository;
    private final Clock clock;

    public ApprovalService(
            OrganizationAuthorizationService authorizationService,
            ApprovalRequestRepository approvalRequestRepository,
            MarketingStrategyRepository strategyRepository,
            CampaignRepository campaignRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            ContentPieceRepository contentPieceRepository,
            Clock clock
    ) {
        this.authorizationService = authorizationService;
        this.approvalRequestRepository = approvalRequestRepository;
        this.strategyRepository = strategyRepository;
        this.campaignRepository = campaignRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.contentPieceRepository = contentPieceRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequest> list(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return approvalRequestRepository.findByOrganization(organizationId);
    }

    @Transactional(readOnly = true)
    public List<ApprovalRequest> pending(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return approvalRequestRepository.findPending(organizationId);
    }

    @Transactional
    public ApprovalRequest decide(
            OrganizationId organizationId,
            UserId userId,
            UUID approvalId,
            boolean approved,
            String note
    ) {
        authorizationService.requireWriter(organizationId, userId);
        ApprovalRequest request = approvalRequestRepository.findById(approvalId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found"));
        Instant now = clock.instant();
        request.decide(approved, userId, note, now);
        approvalRequestRepository.save(request);
        if (request.type() == ApprovalType.STRATEGY) {
            applyStrategy(organizationId, request.subjectId(), approved, now);
        } else if (request.type() == ApprovalType.CONTENT_PIECE) {
            applyContent(organizationId, request.subjectId(), approved, now);
        }
        return request;
    }

    private void applyStrategy(OrganizationId organizationId, UUID strategyId, boolean approved, Instant now) {
        MarketingStrategy strategy = strategyRepository.findById(strategyId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Strategy not found"));
        if (approved) {
            strategyRepository.findByOrganization(organizationId).stream()
                    .filter(existing -> existing.status() == StrategyStatus.APPROVED)
                    .forEach(existing -> {
                        existing.supersede(now);
                        strategyRepository.save(existing);
                    });
            strategy.approve(now);
            strategyRepository.save(strategy);
            campaignRepository.save(Campaign.create(
                    organizationId,
                    strategy.id(),
                    "Plan de canales",
                    strategy.channelsJson(),
                    now
            ));
            knowledgeItemRepository.save(KnowledgeItem.create(
                    organizationId,
                    KnowledgeKind.DECISION,
                    "Estrategia aprobada",
                    strategy.positioning(),
                    "approval",
                    100,
                    now
            ));
        } else {
            strategy.reject(now);
            strategyRepository.save(strategy);
        }
    }

    private void applyContent(OrganizationId organizationId, UUID pieceId, boolean approved, Instant now) {
        ContentPiece piece = contentPieceRepository.findById(pieceId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));
        if (approved) {
            piece.approve(now);
        } else {
            piece.reject(now);
        }
        contentPieceRepository.save(piece);
    }
}
