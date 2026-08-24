package com.mavora.agents.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class WorkflowExecution {

    private final UUID id;
    private final OrganizationId organizationId;
    private final WorkflowType type;
    private WorkflowStatus status;
    private final String inputJson;
    private String outputJson;
    private String errorMessage;
    private final UserId createdBy;
    private final Instant createdAt;
    private Instant startedAt;
    private Instant finishedAt;
    private long version;

    private WorkflowExecution(
            UUID id,
            OrganizationId organizationId,
            WorkflowType type,
            WorkflowStatus status,
            String inputJson,
            String outputJson,
            String errorMessage,
            UserId createdBy,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        this.inputJson = inputJson;
        this.outputJson = outputJson;
        this.errorMessage = errorMessage;
        this.createdBy = Objects.requireNonNull(createdBy);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.version = version;
    }

    public static WorkflowExecution enqueue(
            OrganizationId organizationId,
            WorkflowType type,
            String inputJson,
            UserId createdBy,
            Instant now
    ) {
        return new WorkflowExecution(
                UUID.randomUUID(),
                organizationId,
                type,
                WorkflowStatus.QUEUED,
                inputJson,
                null,
                null,
                createdBy,
                now,
                null,
                null,
                0
        );
    }

    public static WorkflowExecution reconstitute(
            UUID id,
            OrganizationId organizationId,
            WorkflowType type,
            WorkflowStatus status,
            String inputJson,
            String outputJson,
            String errorMessage,
            UserId createdBy,
            Instant createdAt,
            Instant startedAt,
            Instant finishedAt,
            long version
    ) {
        return new WorkflowExecution(
                id, organizationId, type, status, inputJson, outputJson, errorMessage,
                createdBy, createdAt, startedAt, finishedAt, version
        );
    }

    public void markRunning(Instant now) {
        if (status != WorkflowStatus.QUEUED) {
            throw new DomainException("Workflow cannot start from " + status);
        }
        this.status = WorkflowStatus.RUNNING;
        this.startedAt = now;
    }

    public void succeed(String outputJson, Instant now) {
        this.status = WorkflowStatus.SUCCEEDED;
        this.outputJson = outputJson;
        this.finishedAt = now;
        this.errorMessage = null;
    }

    public void fail(String errorMessage, Instant now) {
        this.status = WorkflowStatus.FAILED;
        this.errorMessage = truncate(errorMessage);
        this.finishedAt = now;
    }

    private static String truncate(String message) {
        if (message == null) {
            return "Workflow failed";
        }
        String trimmed = message.trim();
        return trimmed.length() > 1000 ? trimmed.substring(0, 1000) : trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public WorkflowType type() {
        return type;
    }

    public WorkflowStatus status() {
        return status;
    }

    public String inputJson() {
        return inputJson;
    }

    public String outputJson() {
        return outputJson;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public UserId createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant finishedAt() {
        return finishedAt;
    }

    public long version() {
        return version;
    }
}
