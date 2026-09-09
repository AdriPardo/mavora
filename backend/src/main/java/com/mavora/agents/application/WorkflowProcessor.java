package com.mavora.agents.application;

import com.mavora.agents.domain.AgentRun;
import com.mavora.agents.domain.AgentRunRepository;
import com.mavora.agents.domain.AgentType;
import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowExecutionRepository;
import com.mavora.agents.domain.WorkflowType;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowProcessor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProcessor.class);

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final AgentRunRepository agentRunRepository;
    private final List<WorkflowHandler> handlers;
    private final LlmGateway llmGateway;
    private final Clock clock;

    public WorkflowProcessor(
            WorkflowExecutionRepository workflowExecutionRepository,
            AgentRunRepository agentRunRepository,
            List<WorkflowHandler> handlers,
            LlmGateway llmGateway,
            Clock clock
    ) {
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.agentRunRepository = agentRunRepository;
        this.handlers = handlers;
        this.llmGateway = llmGateway;
        this.clock = clock;
    }

    @Transactional
    public boolean processNext() {
        var claimed = workflowExecutionRepository.lockNextQueued();
        if (claimed.isEmpty()) {
            return false;
        }
        WorkflowExecution execution = claimed.get();
        execution.markRunning(clock.instant());
        workflowExecutionRepository.save(execution);

        WorkflowHandler handler = handlers.stream()
                .filter(candidate -> candidate.supports(execution.type()))
                .findFirst()
                .orElse(null);
        AgentType agentType = agentFor(execution.type());
        AgentRun run = AgentRun.start(
                execution.organizationId(),
                execution.id(),
                agentType,
                execution.inputJson(),
                clock.instant()
        );
        try {
            if (handler == null) {
                throw new IllegalStateException("No handler for " + execution.type());
            }
            HandlerResult result = handler.execute(execution, llmGateway);
            execution.succeed(result.outputJson(), clock.instant());
            LlmCompletion completion = result.completion();
            if (completion == null) {
                run.succeed(llmGateway.defaultModel(), 0, 0, 0, result.outputJson(), clock.instant());
            } else {
                run.succeed(
                        completion.model(),
                        completion.promptTokens(),
                        completion.completionTokens(),
                        completion.costCents(),
                        completion.content(),
                        clock.instant()
                );
            }
            workflowExecutionRepository.save(execution);
            agentRunRepository.save(run);
            return true;
        } catch (RuntimeException exception) {
            log.warn("Workflow {} failed: {}", execution.id(), exception.getMessage());
            execution.fail(safeMessage(exception), clock.instant());
            run.fail(safeMessage(exception), clock.instant());
            workflowExecutionRepository.save(execution);
            agentRunRepository.save(run);
            return true;
        }
    }

    private static AgentType agentFor(WorkflowType type) {
        return switch (type) {
            case CMO_STRATEGY -> AgentType.CMO;
            case MARKET_RESEARCH -> AgentType.RESEARCHER;
            case CONTENT_CYCLE -> AgentType.CONTENT;
            case SOCIAL_PLAN -> AgentType.SOCIAL;
            case ANALYTICS_CYCLE -> AgentType.ANALYST;
            case INSTAGRAM_WEEK -> AgentType.INSTAGRAM;
        };
    }

    private static String safeMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Workflow failed";
        }
        return message;
    }
}
