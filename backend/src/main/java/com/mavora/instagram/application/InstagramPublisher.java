package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramFormat;
import com.mavora.instagram.domain.InstagramProvider;
import java.util.List;

public interface InstagramPublisher {

    boolean supports(InstagramProvider provider);

    PublishResult publish(PublishCommand command);

    record PublishCommand(
            String igUserId,
            String accessToken,
            InstagramFormat format,
            String caption,
            List<String> imageUrls,
            String videoUrl,
            String graphVersion
    ) {
    }

    record PublishResult(String igMediaId) {
    }
}
