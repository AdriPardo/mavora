package com.mavora.research.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "competitors")
public class CompetitorEntity extends OrgOwnedEntity {
    @Column(nullable = false, length = 160) private String name;
    @Column(length = 2048) private String url;
    @Column(nullable = false, columnDefinition = "TEXT") private String notes;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
