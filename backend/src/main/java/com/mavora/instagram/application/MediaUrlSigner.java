package com.mavora.instagram.application;

import java.util.UUID;

public interface MediaUrlSigner {

    SignedUrl sign(UUID assetId);

    boolean verify(UUID assetId, long expiresEpochSeconds, String signature);

    record SignedUrl(String url, long expiresEpochSeconds, String signature) {
    }
}
