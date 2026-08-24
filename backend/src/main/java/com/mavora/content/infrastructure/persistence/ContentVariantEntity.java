package com.mavora.content.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "content_variants")
public class ContentVariantEntity extends OrgOwnedEntity {
    @Column(name = "piece_id", nullable = false) private UUID pieceId;
    @Column(nullable = false, length = 64) private String channel;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    public UUID getPieceId() { return pieceId; }
    public void setPieceId(UUID pieceId) { this.pieceId = pieceId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}
