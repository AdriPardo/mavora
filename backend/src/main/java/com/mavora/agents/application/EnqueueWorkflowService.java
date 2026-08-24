package com.mavora.agents.application;

import com.mavora.agents.domain.WorkflowExecution;
import com.mavora.agents.domain.WorkflowExecutionRepository;
import com.mavora.agents.domain.WorkflowType;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class EnqueueWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(EnqueueWorkflowService.class);
    private final OrganizationAuthorizationService authorizationService;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final WorkflowProcessor workflowProcessor;
    private final Clock clock;
    private final boolean inline;

    public EnqueueWorkflowService(
            OrganizationAuthorizationService authorizationService,
            WorkflowExecutionRepository workflowExecutionRepository,
            WorkflowProcessor workflowProcessor,
            Clock clock,
            @Value("${mavora.jobs.inline:false}") boolean inline
    ) {
        this.authorizationService = authorizationService;
        this.workflowExecutionRepository = workflowExecutionRepository;
        this.workflowProcessor = workflowProcessor;
        this.clock = clock;
        this.inline = inline;
    }

    @Transactional
    public WorkflowExecution enqueue(OrganizationId organizationId, UserId userId, WorkflowType type, String inputJson) {
        authorizationService.requireWriter(organizationId, userId);
        if (workflowExecutionRepository.hasActive(organizationId, type)) {
            throw new DomainException(
                    DomainException.ErrorType.CONFLICT,
                    "Workflow already running",
                    "A job of this type is already queued or running"
            );
        }
        WorkflowExecution saved = workflowExecutionRepository.save(
                WorkflowExecution.enqueue(organizationId, type, inputJson, userId, clock.instant())
        );
        if (inline) {
            workflowProcessor.processNext();
        } else if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        workflowProcessor.processNext();
                    } catch (RuntimeException exception) {
                        log.warn("Workflow processing failed: {}", exception.getMessage());
                    }
                }
            });
        }
        return saved;
    }
}
