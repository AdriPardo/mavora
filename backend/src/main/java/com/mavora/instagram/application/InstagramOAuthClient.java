package com.mavora.instagram.application;

import java.time.Instant;

public interface InstagramOAuthClient {

    String authorizeUrl(MetaAppCredentials credentials, String state);

    ConnectedAccount exchange(MetaAppCredentials credentials, String code);

    record ConnectedAccount(
            String igUserId,
            String username,
            String pageId,
            String accessToken,
            Instant expiresAt
    ) {
    }
}
