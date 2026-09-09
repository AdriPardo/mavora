package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramFormat;
import java.util.List;

public interface InstagramPublisher {

    PublishResult publish(PublishCommand command);

    record PublishCommand(
            String igUserId,
            String accessToken,
            InstagramFormat format,
            String caption,
            List<String> imageUrls,
            String videoUrl
    ) {
    }

    record PublishResult(String igMediaId) {
    }
}
