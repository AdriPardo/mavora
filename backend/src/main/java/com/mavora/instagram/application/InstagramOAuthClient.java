package com.mavora.instagram.application;

import java.time.Instant;

public interface InstagramOAuthClient {

    String authorizeUrl(String state);

    ConnectedAccount exchange(String code);

    record ConnectedAccount(
            String igUserId,
            String username,
            String pageId,
            String accessToken,
            Instant expiresAt
    ) {
    }
}
