package com.mavora.agents.api;

import com.mavora.agents.application.AgentQueryService;
import com.mavora.agents.application.EnqueueWorkflowService;
import com.mavora.agents.domain.AgentRun;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Agents")
public class WorkflowController {

    private final EnqueueWorkflowService enqueueWorkflowService;
    private final AgentQueryService agentQueryService;

    public WorkflowController(EnqueueWorkflowService enqueueWorkflowService, AgentQueryService agentQueryService) {
        this.enqueueWorkflowService = enqueueWorkflowService;
        this.agentQueryService = agentQueryService;
    }

    @PostMapping("/workflows/{type}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public WorkflowResponse enqueue(
            @PathVariable UUID organizationId,
            @PathVariable WorkflowType type,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        WorkflowExecution execution = enqueueWorkflowService.enqueue(
                new OrganizationId(organizationId), principal.userId(), type, "{}"
        );
        return WorkflowResponse.from(execution);
    }

    @GetMapping("/workflows/{workflowId}")
    public WorkflowResponse get(
            @PathVariable UUID organizationId,
            @PathVariable UUID workflowId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return WorkflowResponse.from(agentQueryService.getWorkflow(
                new OrganizationId(organizationId), principal.userId(), workflowId
        ));
    }

    @GetMapping("/workflows")
    public WorkflowListResponse list(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new WorkflowListResponse(agentQueryService.recentWorkflows(
                new OrganizationId(organizationId), principal.userId()
        ).stream().map(WorkflowResponse::from).toList());
    }

    @GetMapping("/agent-runs")
    public RunListResponse runs(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return new RunListResponse(agentQueryService.runs(
                new OrganizationId(organizationId), principal.userId()
        ).stream().map(RunResponse::from).toList());
    }

    @GetMapping("/usage")
    public AgentQueryService.Usage usage(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        return agentQueryService.usage(new OrganizationId(organizationId), principal.userId());
    }

    public record WorkflowListResponse(List<WorkflowResponse> items) {
    }

    public record WorkflowResponse(
            UUID id,
            String type,
            String status,
            String errorMessage,
            Instant createdAt,
            Instant finishedAt
    ) {
        public static WorkflowResponse from(WorkflowExecution execution) {
            return new WorkflowResponse(
                    execution.id(),
                    execution.type().name(),
                    execution.status().name(),
                    execution.errorMessage(),
                    execution.createdAt(),
                    execution.finishedAt()
            );
        }
    }

    public record RunListResponse(List<RunResponse> items) {
    }

    public record RunResponse(
            UUID id,
            UUID workflowId,
            String agentType,
            String status,
            String model,
            int promptTokens,
            int completionTokens,
            long costCents,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt
    ) {
        public static RunResponse from(AgentRun run) {
            return new RunResponse(
                    run.id(),
                    run.workflowId(),
                    run.agentType().name(),
                    run.status().name(),
                    run.model(),
                    run.promptTokens(),
                    run.completionTokens(),
                    run.costCents(),
                    run.errorMessage(),
                    run.startedAt(),
                    run.finishedAt()
            );
        }
    }
}
