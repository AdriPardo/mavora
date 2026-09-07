package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramProvider;

public interface InstagramProfileReader {

    boolean supports(InstagramProvider provider);

    InstagramProfileSnapshot read(InstagramAccount account, String accessToken, String graphVersion);
}
