package com.mavora.instagram.domain;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class InstagramSlot {

    private final UUID id;
    private final OrganizationId organizationId;
    private final InstagramFormat format;
    private InstagramSlotStatus status;
    private final Instant scheduledAt;
    private final String hook;
    private final String caption;
    private final String cta;
    private final List<String> hashtags;
    private final List<UUID> mediaAssetIds;
    private String igMediaId;
    private String errorMessage;
    private Instant publishedAt;
    private final Instant createdAt;
    private long version;

    private InstagramSlot(
            UUID id,
            OrganizationId organizationId,
            InstagramFormat format,
            InstagramSlotStatus status,
            Instant scheduledAt,
            String hook,
            String caption,
            String cta,
            List<String> hashtags,
            List<UUID> mediaAssetIds,
            String igMediaId,
            String errorMessage,
            Instant publishedAt,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.format = Objects.requireNonNull(format);
        this.status = Objects.requireNonNull(status);
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.hook = requireText(hook, "hook", 1, 300);
        this.caption = requireText(caption, "caption", 1, 2200);
        this.cta = requireText(cta, "cta", 1, 300);
        this.hashtags = List.copyOf(hashtags == null ? List.of() : hashtags);
        this.mediaAssetIds = List.copyOf(Objects.requireNonNull(mediaAssetIds));
        if (this.mediaAssetIds.isEmpty()) {
            throw new IllegalArgumentException("mediaAssetIds is required");
        }
        this.igMediaId = igMediaId;
        this.errorMessage = errorMessage;
        this.publishedAt = publishedAt;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static InstagramSlot schedule(
            OrganizationId organizationId,
            InstagramFormat format,
            Instant scheduledAt,
            String hook,
            String caption,
            String cta,
            List<String> hashtags,
            List<UUID> mediaAssetIds,
            Instant now
    ) {
        return new InstagramSlot(
                UUID.randomUUID(),
                organizationId,
                format,
                InstagramSlotStatus.SCHEDULED,
                scheduledAt,
                hook,
                caption,
                cta,
                hashtags,
                mediaAssetIds,
                null,
                null,
                null,
                now,
                0
        );
    }

    public static InstagramSlot reconstitute(
            UUID id,
            OrganizationId organizationId,
            InstagramFormat format,
            InstagramSlotStatus status,
            Instant scheduledAt,
            String hook,
            String caption,
            String cta,
            List<String> hashtags,
            List<UUID> mediaAssetIds,
            String igMediaId,
            String errorMessage,
            Instant publishedAt,
            Instant createdAt,
            long version
    ) {
        return new InstagramSlot(
                id, organizationId, format, status, scheduledAt, hook, caption, cta, hashtags,
                mediaAssetIds, igMediaId, errorMessage, publishedAt, createdAt, version
        );
    }

    public void claimForPublish() {
        if (status != InstagramSlotStatus.SCHEDULED) {
            throw new DomainException("Only a scheduled slot can be published");
        }
        this.status = InstagramSlotStatus.PUBLISHING;
        this.errorMessage = null;
    }

    public void markPublished(String igMediaId, Instant now) {
        if (status != InstagramSlotStatus.PUBLISHING) {
            throw new DomainException("Only a publishing slot can be marked published");
        }
        this.status = InstagramSlotStatus.PUBLISHED;
        this.igMediaId = requireText(igMediaId, "igMediaId", 1, 128);
        this.publishedAt = now;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        if (status != InstagramSlotStatus.PUBLISHING && status != InstagramSlotStatus.SCHEDULED) {
            throw new DomainException("Only a scheduled or publishing slot can fail");
        }
        this.status = InstagramSlotStatus.FAILED;
        String trimmed = errorMessage == null || errorMessage.isBlank() ? "Publish failed" : errorMessage.trim();
        this.errorMessage = trimmed.length() > 1000 ? trimmed.substring(0, 1000) : trimmed;
    }

    public void cancel() {
        if (status != InstagramSlotStatus.SCHEDULED) {
            return;
        }
        this.status = InstagramSlotStatus.CANCELLED;
    }

    private static String requireText(String value, String field, int min, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String trimmed = value.trim();
        if (trimmed.length() < min || trimmed.length() > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + " characters");
        }
        return trimmed;
    }

    public UUID id() {
        return id;
    }

    public OrganizationId organizationId() {
        return organizationId;
    }

    public InstagramFormat format() {
        return format;
    }

    public InstagramSlotStatus status() {
        return status;
    }

    public Instant scheduledAt() {
        return scheduledAt;
    }

    public String hook() {
        return hook;
    }

    public String caption() {
        return caption;
    }

    public String cta() {
        return cta;
    }

    public List<String> hashtags() {
        return hashtags;
    }

    public List<UUID> mediaAssetIds() {
        return mediaAssetIds;
    }

    public String igMediaId() {
        return igMediaId;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public Instant publishedAt() {
        return publishedAt;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
