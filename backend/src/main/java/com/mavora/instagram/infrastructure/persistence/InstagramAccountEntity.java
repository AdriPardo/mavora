package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.InstagramProvider;
import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "instagram_accounts")
public class InstagramAccountEntity extends OrgOwnedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InstagramProvider provider;

    @Column(name = "ig_user_id", nullable = false, length = 64)
    private String igUserId;

    @Column(nullable = false, length = 120)
    private String username;

    @Column(name = "page_id", length = 64)
    private String pageId;

    @Column(name = "token_ciphertext", columnDefinition = "TEXT")
    private String tokenCiphertext;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "autonomy_enabled", nullable = false)
    private boolean autonomyEnabled;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "disconnected_at")
    private Instant disconnectedAt;

    public InstagramProvider getProvider() {
        return provider;
    }

    public void setProvider(InstagramProvider provider) {
        this.provider = provider;
    }

    public String getIgUserId() {
        return igUserId;
    }

    public void setIgUserId(String igUserId) {
        this.igUserId = igUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getTokenCiphertext() {
        return tokenCiphertext;
    }

    public void setTokenCiphertext(String tokenCiphertext) {
        this.tokenCiphertext = tokenCiphertext;
    }

    public Instant getTokenExpiresAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiresAt(Instant tokenExpiresAt) {
        this.tokenExpiresAt = tokenExpiresAt;
    }

    public boolean isAutonomyEnabled() {
        return autonomyEnabled;
    }

    public void setAutonomyEnabled(boolean autonomyEnabled) {
        this.autonomyEnabled = autonomyEnabled;
    }

    public Instant getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(Instant connectedAt) {
        this.connectedAt = connectedAt;
    }

    public Instant getDisconnectedAt() {
        return disconnectedAt;
    }

    public void setDisconnectedAt(Instant disconnectedAt) {
        this.disconnectedAt = disconnectedAt;
    }
}
