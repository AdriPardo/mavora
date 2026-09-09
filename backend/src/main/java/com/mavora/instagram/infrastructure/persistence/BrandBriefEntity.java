package com.mavora.instagram.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "brand_briefs")
public class BrandBriefEntity extends OrgOwnedEntity {

    @Column(length = 500)
    private String voice;

    @Column(length = 2000)
    private String offer;

    @Column(length = 300)
    private String cta;

    @Column(length = 500)
    private String audience;

    @Column(name = "extra_notes", length = 4000)
    private String extraNotes;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    public String getCta() {
        return cta;
    }

    public void setCta(String cta) {
        this.cta = cta;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getExtraNotes() {
        return extraNotes;
    }

    public void setExtraNotes(String extraNotes) {
        this.extraNotes = extraNotes;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
