package com.mavora.instagram.application;

public record MetaAppCredentials(
        String appId,
        String appSecret,
        String redirectUri,
        String graphVersion,
        String source
) {
    public boolean isReady() {
        return appId != null && !appId.isBlank() && appSecret != null && !appSecret.isBlank()
                && redirectUri != null && !redirectUri.isBlank();
    }
}
