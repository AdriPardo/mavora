package com.mavora.knowledge.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class KnowledgeItem {

    private final UUID id;
    private final OrganizationId organizationId;
    private final KnowledgeKind kind;
    private final String title;
    private final String body;
    private final String source;
    private final Integer confidence;
    private final Instant createdAt;
    private final long version;

    private KnowledgeItem(
            UUID id,
            OrganizationId organizationId,
            KnowledgeKind kind,
            String title,
            String body,
            String source,
            Integer confidence,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.kind = Objects.requireNonNull(kind);
        this.title = requireTitle(title);
        this.body = requireBody(body);
        this.source = source == null || source.isBlank() ? null : source.trim();
        if (confidence != null && (confidence < 0 || confidence > 100)) {
            throw new IllegalArgumentException("confidence must be between 0 and 100");
        }
        this.confidence = confidence;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static KnowledgeItem create(
            OrganizationId organizationId,
            KnowledgeKind kind,
            String title,
            String body,
            String source,
            Integer confidence,
            Instant now
    ) {
        return new KnowledgeItem(
                UUID.randomUUID(), organizationId, kind, title, body, source, confidence, now, 0
        );
    }

    public static KnowledgeItem reconstitute(
            UUID id,
            OrganizationId organizationId,
            KnowledgeKind kind,
            String title,
            String body,
            String source,
            Integer confidence,
            Instant createdAt,
            long version
    ) {
        return new KnowledgeItem(id, organizationId, kind, title, body, source, confidence, createdAt, version);
    }

    private static String requireTitle(String title) {
        if (title == null) {
            throw new IllegalArgumentException("title is required");
        }
        String trimmed = title.trim();
        if (trimmed.length() < 2 || trimmed.length() > 200) {
            throw new IllegalArgumentException("title must be between 2 and 200 characters");
        }
        return trimmed;
    }

    private static String requireBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("body is required");
        }
        String trimmed = body.trim();
        if (trimmed.length() > 20_000) {
            return trimmed.substring(0, 20_000);
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public KnowledgeKind kind() {
        return kind;
    }

    public String title() {
        return title;
    }

    public String body() {
        return body;
    }

    public String source() {
        return source;
    }

    public Integer confidence() {
        return confidence;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
