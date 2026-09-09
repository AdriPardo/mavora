package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MediaAsset {

    private final UUID id;
    private final OrganizationId organizationId;
    private final UUID productId;
    private final MediaKind kind;
    private final String filename;
    private final String contentType;
    private final String storagePath;
    private final long byteSize;
    private final String captionHint;
    private final Instant createdAt;
    private final long version;

    private MediaAsset(
            UUID id,
            OrganizationId organizationId,
            UUID productId,
            MediaKind kind,
            String filename,
            String contentType,
            String storagePath,
            long byteSize,
            String captionHint,
            Instant createdAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id);
        this.organizationId = Objects.requireNonNull(organizationId);
        this.productId = productId;
        this.kind = Objects.requireNonNull(kind);
        this.filename = requireText(filename, "filename", 1, 255);
        this.contentType = requireText(contentType, "contentType", 1, 120);
        this.storagePath = requireText(storagePath, "storagePath", 1, 1024);
        if (byteSize <= 0) {
            throw new IllegalArgumentException("byteSize must be positive");
        }
        this.byteSize = byteSize;
        this.captionHint = captionHint == null || captionHint.isBlank() ? null : captionHint.trim();
        if (this.captionHint != null && this.captionHint.length() > 500) {
            throw new IllegalArgumentException("captionHint is too long");
        }
        this.createdAt = Objects.requireNonNull(createdAt);
        this.version = version;
    }

    public static MediaAsset create(
            OrganizationId organizationId,
            UUID productId,
            MediaKind kind,
            String filename,
            String contentType,
            String storagePath,
            long byteSize,
            String captionHint,
            Instant now
    ) {
        return new MediaAsset(
                UUID.randomUUID(), organizationId, productId, kind, filename, contentType,
                storagePath, byteSize, captionHint, now, 0
        );
    }

    public static MediaAsset reconstitute(
            UUID id,
            OrganizationId organizationId,
            UUID productId,
            MediaKind kind,
            String filename,
            String contentType,
            String storagePath,
            long byteSize,
            String captionHint,
            Instant createdAt,
            long version
    ) {
        return new MediaAsset(
                id, organizationId, productId, kind, filename, contentType, storagePath,
                byteSize, captionHint, createdAt, version
        );
    }

    public boolean isImage() {
        return kind == MediaKind.IMAGE;
    }

    public boolean isVideo() {
        return kind == MediaKind.VIDEO;
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

    public UUID productId() {
        return productId;
    }

    public MediaKind kind() {
        return kind;
    }

    public String filename() {
        return filename;
    }

    public String contentType() {
        return contentType;
    }

    public String storagePath() {
        return storagePath;
    }

    public long byteSize() {
        return byteSize;
    }

    public String captionHint() {
        return captionHint;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public long version() {
        return version;
    }
}
