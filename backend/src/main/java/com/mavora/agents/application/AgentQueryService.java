package com.mavora.agents.application;

import com.mavora.agents.domain.AgentRun;
import com.mavora.agents.domain.AgentRunRepository;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowExecutionRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AgentRunRepository agentRunRepository;
    private final LlmGateway llmGateway;

    public AgentQueryService(
            OrganizationAuthorizationService authorizationService,
            WorkflowExecutionRepository workflowExecutionRepository,
            AgentRunRepository agentRunRepository,
            LlmGateway llmGateway
    ) {
        this.authorizationService = authorizationService;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.agentRunRepository = agentRunRepository;
        this.llmGateway = llmGateway;
    }

    @Transactional(readOnly = true)
    public WorkflowExecution getWorkflow(OrganizationId organizationId, UserId userId, UUID workflowId) {
        authorizationService.requireMember(organizationId, userId);
        return workflowExecutionRepository.findById(workflowId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found"));
    }

    @Transactional(readOnly = true)
    public List<WorkflowExecution> recentWorkflows(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return workflowExecutionRepository.findRecent(organizationId, 20);
    }

    @Transactional(readOnly = true)
    public List<AgentRun> runs(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return agentRunRepository.findByOrganization(organizationId);
    }

    @Transactional(readOnly = true)
    public Usage usage(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return new Usage(llmGateway.spentThisMonth(organizationId), llmGateway.monthlyBudgetCents());
    }

    public record Usage(long spentCentsThisMonth, long monthlyBudgetCents) {
    }
}
