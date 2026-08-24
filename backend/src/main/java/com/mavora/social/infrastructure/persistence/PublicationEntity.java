package com.mavora.social.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import com.mavora.social.domain.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "publications")
public class PublicationEntity extends OrgOwnedEntity {
    @Column(name = "piece_id") private UUID pieceId;
    @Column(nullable = false, length = 64) private String channel;
    @Column(name = "copy", nullable = false, columnDefinition = "TEXT") private String copy;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private PublicationStatus status;
    @Column(name = "published_at") private Instant publishedAt;
    public UUID getPieceId() { return pieceId; }
    public void setPieceId(UUID pieceId) { this.pieceId = pieceId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getCopy() { return copy; }
    public void setCopy(String copy) { this.copy = copy; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
}
