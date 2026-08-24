package com.mavora.workspace.api;

import com.mavora.agents.api.WorkflowController.RunResponse;
import com.mavora.agents.api.WorkflowController.WorkflowResponse;
import com.mavora.agents.application.AgentQueryService;
import com.mavora.company.api.CompanyController.CompanyResponse;
import com.mavora.company.api.CompanyController.GoalResponse;
import com.mavora.company.api.CompanyController.ProductResponse;
import com.mavora.company.application.CompanyProfileService;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.strategy.api.StrategyController.StrategyResponse;
import com.mavora.workspace.application.WorkspaceQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}/workspace", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Workspace")
public class WorkspaceController {

    private final WorkspaceQueryService workspaceQueryService;

    public WorkspaceController(WorkspaceQueryService workspaceQueryService) {
        this.workspaceQueryService = workspaceQueryService;
    }

    @GetMapping
    public WorkspaceResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        var view = workspaceQueryService.get(new OrganizationId(organizationId), principal.userId());
        CompanyResponse company = null;
        if (view.company() != null) {
            CompanyProfileService.CompanyProfile profile = view.company();
            company = new CompanyResponse(
                    profile.company().id(),
                    profile.company().name(),
                    profile.company().websiteUrl(),
                    profile.company().description(),
                    profile.company().market(),
                    profile.products().stream().map(ProductResponse::from).toList(),
                    profile.company().updatedAt()
            );
        }
        return new WorkspaceResponse(
                company,
                view.goal() == null ? null : GoalResponse.from(view.goal()),
                view.strategy() == null ? null : StrategyResponse.from(view.strategy()),
                view.pendingApprovals(),
                view.knowledgeCount(),
                view.contentCount(),
                view.campaignCount(),
                view.publicationCount(),
                view.workflows().stream().map(WorkflowResponse::from).toList(),
                view.runs().stream().map(RunResponse::from).toList(),
                view.usage()
        );
    }

    public record WorkspaceResponse(
            CompanyResponse company,
            GoalResponse goal,
            StrategyResponse strategy,
            long pendingApprovals,
            int knowledgeCount,
            int contentCount,
            int campaignCount,
            int publicationCount,
            List<WorkflowResponse> workflows,
            List<RunResponse> runs,
            AgentQueryService.Usage usage
    ) {
    }
}
