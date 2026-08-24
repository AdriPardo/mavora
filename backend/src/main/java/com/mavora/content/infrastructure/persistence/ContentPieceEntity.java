package com.mavora.content.infrastructure.persistence;

import com.mavora.content.domain.PieceStatus;
import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_pieces")
public class ContentPieceEntity extends OrgOwnedEntity {
    @Column(name = "idea_id", nullable = false) private UUID ideaId;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private PieceStatus status;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    public UUID getIdeaId() { return ideaId; }
    public void setIdeaId(UUID ideaId) { this.ideaId = ideaId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public PieceStatus getStatus() { return status; }
    public void setStatus(PieceStatus status) { this.status = status; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
