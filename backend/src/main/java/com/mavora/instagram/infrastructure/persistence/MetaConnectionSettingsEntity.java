package com.mavora.instagram.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "meta_connection_settings")
public class MetaConnectionSettingsEntity extends OrgOwnedEntity {

    @Column(name = "app_id", length = 64)
    private String appId;

    @Column(name = "app_secret_ciphertext", columnDefinition = "TEXT")
    private String appSecretCiphertext;

    @Column(name = "redirect_uri", length = 500)
    private String redirectUri;

    @Column(name = "graph_version", nullable = false, length = 16)
    private String graphVersion;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecretCiphertext() {
        return appSecretCiphertext;
    }

    public void setAppSecretCiphertext(String appSecretCiphertext) {
        this.appSecretCiphertext = appSecretCiphertext;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getGraphVersion() {
        return graphVersion;
    }

    public void setGraphVersion(String graphVersion) {
        this.graphVersion = graphVersion;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
