package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.InstagramFormat;
import com.mavora.instagram.domain.InstagramSlotStatus;
import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "instagram_slots")
public class InstagramSlotEntity extends OrgOwnedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InstagramFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InstagramSlotStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false, length = 300)
    private String hook;

    @Column(nullable = false, length = 2200)
    private String caption;

    @Column(nullable = false, length = 300)
    private String cta;

    @Column(name = "hashtags_json", nullable = false, columnDefinition = "TEXT")
    private String hashtagsJson;

    @Column(name = "media_asset_ids_json", nullable = false, columnDefinition = "TEXT")
    private String mediaAssetIdsJson;

    @Column(name = "ig_media_id", length = 128)
    private String igMediaId;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "published_at")
    private Instant publishedAt;

    public InstagramFormat getFormat() {
        return format;
    }

    public void setFormat(InstagramFormat format) {
        this.format = format;
    }

    public InstagramSlotStatus getStatus() {
        return status;
    }

    public void setStatus(InstagramSlotStatus status) {
        this.status = status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getHook() {
        return hook;
    }

    public void setHook(String hook) {
        this.hook = hook;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }

    public String getHashtagsJson() {
        return hashtagsJson;
    }

    public void setHashtagsJson(String hashtagsJson) {
        this.hashtagsJson = hashtagsJson;
    }

    public String getMediaAssetIdsJson() {
        return mediaAssetIdsJson;
    }

    public void setMediaAssetIdsJson(String mediaAssetIdsJson) {
        this.mediaAssetIdsJson = mediaAssetIdsJson;
    }

    public String getIgMediaId() {
        return igMediaId;
    }

    public void setIgMediaId(String igMediaId) {
        this.igMediaId = igMediaId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }
}
