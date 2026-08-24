package com.mavora.content.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_ideas")
public class ContentIdeaEntity extends OrgOwnedEntity {
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "TEXT") private String angle;
    @Column(length = 120) private String pillar;
    @Column(nullable = false, length = 32) private String status;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAngle() { return angle; }
    public void setAngle(String angle) { this.angle = angle; }
    public String getPillar() { return pillar; }
    public void setPillar(String pillar) { this.pillar = pillar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
