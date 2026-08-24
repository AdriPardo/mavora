package com.mavora.knowledge.infrastructure.persistence;

import com.mavora.knowledge.domain.KnowledgeKind;
import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_items")
public class KnowledgeItemEntity extends OrgOwnedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KnowledgeKind kind;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 2048)
    private String source;

    private Integer confidence;

    public KnowledgeKind getKind() {
        return kind;
    }

    public void setKind(KnowledgeKind kind) {
        this.kind = kind;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }
}
