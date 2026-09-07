package com.mavora.instagram.infrastructure.publish;

import com.mavora.instagram.application.InstagramPublisher;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mavora.instagram.provider", havingValue = "fake", matchIfMissing = true)
public class FakeInstagramPublisher implements InstagramPublisher {

    @Override
    public PublishResult publish(PublishCommand command) {
        if (command.imageUrls() == null || command.imageUrls().isEmpty()) {
            if (command.videoUrl() == null || command.videoUrl().isBlank()) {
                throw new IllegalStateException("A photo or video is required to publish");
            }
        }
        return new PublishResult("ig_fake_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
    }
}
