package com.mavora.approvals.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ApprovalRequest {

    private final UUID id;
    private final OrganizationId organizationId;
    private final ApprovalType type;
    private final String subjectType;
    private final UUID subjectId;
    private ApprovalStatus status;
    private final String summary;
    private final String payloadJson;
    private final Instant createdAt;
    private Instant decidedAt;
    private UserId decidedBy;
    private String decisionNote;
    private long version;

    private ApprovalRequest(
            UUID id,
            OrganizationId organizationId,
            ApprovalType type,
            String subjectType,
            UUID subjectId,
            ApprovalStatus status,
            String summary,
            String payloadJson,
            Instant createdAt,
            Instant decidedAt,
            UserId decidedBy,
            String decisionNote,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.type = Objects.requireNonNull(type);
        this.subjectType = Objects.requireNonNull(subjectType);
        this.subjectId = Objects.requireNonNull(subjectId);
        this.status = Objects.requireNonNull(status);
        this.summary = Objects.requireNonNull(summary);
        this.payloadJson = Objects.requireNonNull(payloadJson);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.decidedAt = decidedAt;
        this.decidedBy = decidedBy;
        this.decisionNote = decisionNote;
        this.version = version;
    }

    public static ApprovalRequest pending(
            OrganizationId organizationId,
            ApprovalType type,
            String subjectType,
            UUID subjectId,
            String summary,
            String payloadJson,
            Instant now
    ) {
        return new ApprovalRequest(
                UUID.randomUUID(), organizationId, type, subjectType, subjectId,
                ApprovalStatus.PENDING, summary, payloadJson, now, null, null, null, 0
        );
    }

    public static ApprovalRequest reconstitute(
            UUID id,
            OrganizationId organizationId,
            ApprovalType type,
            String subjectType,
            UUID subjectId,
            ApprovalStatus status,
            String summary,
            String payloadJson,
            Instant createdAt,
            Instant decidedAt,
            UserId decidedBy,
            String decisionNote,
            long version
    ) {
        return new ApprovalRequest(
                id, organizationId, type, subjectType, subjectId, status, summary, payloadJson,
                createdAt, decidedAt, decidedBy, decisionNote, version
        );
    }

    public void decide(boolean approved, UserId actor, String note, Instant now) {
        if (status != ApprovalStatus.PENDING) {
            throw new DomainException("This approval has already been decided");
        }
        this.status = approved ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED;
        this.decidedBy = actor;
        this.decidedAt = now;
        this.decisionNote = note == null || note.isBlank() ? null : note.trim();
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public ApprovalType type() {
        return type;
    }

    public String subjectType() {
        return subjectType;
    }

    public UUID subjectId() {
        return subjectId;
    }

    public ApprovalStatus status() {
        return status;
    }

    public String summary() {
        return summary;
    }

    public String payloadJson() {
        return payloadJson;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant decidedAt() {
        return decidedAt;
    }

    public UserId decidedBy() {
        return decidedBy;
    }

    public String decisionNote() {
        return decisionNote;
    }

    public long version() {
        return version;
    }
}
