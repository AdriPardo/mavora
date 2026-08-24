package com.mavora.agents.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AgentRun {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID workflowId;
    private final AgentType agentType;
    private RunStatus status;
    private String model;
    private int promptTokens;
    private int completionTokens;
    private long costCents;
    private final String inputJson;
    private String outputJson;
    private String errorMessage;
    private final Instant startedAt;
    private Instant finishedAt;
    private long version;

    private AgentRun(
            UUID id,
            OrganizationId organizationId,
            UUID workflowId,
            AgentType agentType,
            RunStatus status,
            String model,
            int promptTokens,
            int completionTokens,
            long costCents,
            String inputJson,
            String outputJson,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.workflowId = Objects.requireNonNull(workflowId);
        this.agentType = Objects.requireNonNull(agentType);
        this.status = Objects.requireNonNull(status);
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.costCents = costCents;
        this.inputJson = inputJson;
        this.outputJson = outputJson;
        this.errorMessage = errorMessage;
        this.startedAt = Objects.requireNonNull(startedAt);
        this.finishedAt = finishedAt;
        this.version = version;
    }

    public static AgentRun start(
            OrganizationId organizationId,
            UUID workflowId,
            AgentType agentType,
            String inputJson,
            Instant now
    ) {
        return new AgentRun(
                UUID.randomUUID(),
                organizationId,
                workflowId,
                agentType,
                RunStatus.RUNNING,
                null,
                0,
                0,
                0,
                inputJson,
                null,
                null,
                now,
                null,
                0
        );
    }

    public static AgentRun reconstitute(
            UUID id,
            OrganizationId organizationId,
            UUID workflowId,
            AgentType agentType,
            RunStatus status,
            String model,
            int promptTokens,
            int completionTokens,
            long costCents,
            String inputJson,
            String outputJson,
            String errorMessage,
            Instant startedAt,
            Instant finishedAt,
            long version
    ) {
        return new AgentRun(
                id, organizationId, workflowId, agentType, status, model, promptTokens, completionTokens,
                costCents, inputJson, outputJson, errorMessage, startedAt, finishedAt, version
        );
    }

    public void succeed(String model, int promptTokens, int completionTokens, long costCents, String outputJson, Instant now) {
        this.status = RunStatus.SUCCEEDED;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.costCents = costCents;
        this.outputJson = outputJson;
        this.finishedAt = now;
    }

    public void fail(String errorMessage, Instant now) {
        this.status = RunStatus.FAILED;
        this.errorMessage = errorMessage == null ? "Agent run failed" : errorMessage;
        if (this.errorMessage.length() > 1000) {
            this.errorMessage = this.errorMessage.substring(0, 1000);
        }
        this.finishedAt = now;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public UUID workflowId() {
        return workflowId;
    }

    public AgentType agentType() {
        return agentType;
    }

    public RunStatus status() {
        return status;
    }

    public String model() {
        return model;
    }

    public int promptTokens() {
        return promptTokens;
    }

    public int completionTokens() {
        return completionTokens;
    }

    public long costCents() {
        return costCents;
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
