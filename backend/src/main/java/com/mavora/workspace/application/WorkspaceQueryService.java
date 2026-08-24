package com.mavora.workspace.application;

import com.mavora.agents.application.AgentQueryService;
import com.mavora.agents.domain.AgentRunRepository;
import com.mavora.agents.domain.WorkflowExecutionRepository;
import com.mavora.approvals.domain.ApprovalRequestRepository;
import com.mavora.company.application.CompanyProfileService;
import com.mavora.company.domain.MarketingGoalRepository;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import com.mavora.social.domain.PublicationRepository;
import com.mavora.strategy.domain.CampaignRepository;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final CompanyProfileService companyProfileService;
    private final MarketingGoalRepository goalRepository;
    private final MarketingStrategyRepository strategyRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AgentRunRepository agentRunRepository;
    private final KnowledgeItemRepository knowledgeItemRepository;
    private final ContentPieceRepository contentPieceRepository;
    private final CampaignRepository campaignRepository;
    private final PublicationRepository publicationRepository;
    private final AgentQueryService agentQueryService;

    public WorkspaceQueryService(
            OrganizationAuthorizationService authorizationService,
            CompanyProfileService companyProfileService,
            MarketingGoalRepository goalRepository,
            MarketingStrategyRepository strategyRepository,
            ApprovalRequestRepository approvalRequestRepository,
            WorkflowExecutionRepository workflowExecutionRepository,
            AgentRunRepository agentRunRepository,
            KnowledgeItemRepository knowledgeItemRepository,
            ContentPieceRepository contentPieceRepository,
            CampaignRepository campaignRepository,
            PublicationRepository publicationRepository,
            AgentQueryService agentQueryService
    ) {
        this.authorizationService = authorizationService;
        this.companyProfileService = companyProfileService;
        this.goalRepository = goalRepository;
        this.strategyRepository = strategyRepository;
        this.approvalRequestRepository = approvalRequestRepository;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.agentRunRepository = agentRunRepository;
        this.knowledgeItemRepository = knowledgeItemRepository;
        this.contentPieceRepository = contentPieceRepository;
        this.campaignRepository = campaignRepository;
        this.publicationRepository = publicationRepository;
        this.agentQueryService = agentQueryService;
    }

    @Transactional(readOnly = true)
    public WorkspaceView get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return new WorkspaceView(
                companyProfileService.get(organizationId, userId).orElse(null),
                goalRepository.findActive(organizationId).orElse(null),
                strategyRepository.findLatest(organizationId).orElse(null),
                approvalRequestRepository.countPending(organizationId),
                knowledgeItemRepository.findByOrganization(organizationId).size(),
                contentPieceRepository.findByOrganization(organizationId).size(),
                campaignRepository.findByOrganization(organizationId).size(),
                publicationRepository.findByOrganization(organizationId).size(),
                workflowExecutionRepository.findRecent(organizationId, 8),
                agentRunRepository.findByOrganization(organizationId).stream().limit(8).toList(),
                agentQueryService.usage(organizationId, userId)
        );
    }

    public record WorkspaceView(
            CompanyProfileService.CompanyProfile company,
            com.mavora.company.domain.MarketingGoal goal,
            com.mavora.strategy.domain.MarketingStrategy strategy,
            long pendingApprovals,
            int knowledgeCount,
            int contentCount,
            int campaignCount,
            int publicationCount,
            java.util.List<com.mavora.agents.domain.WorkflowExecution> workflows,
            java.util.List<com.mavora.agents.domain.AgentRun> runs,
            AgentQueryService.Usage usage
    ) {
    }
}
