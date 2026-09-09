package com.mavora.instagram.infrastructure.publish;

import com.mavora.instagram.application.InstagramPublisher;
import com.mavora.instagram.domain.InstagramProvider;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FakeInstagramPublisher implements InstagramPublisher {

    @Override
    public boolean supports(InstagramProvider provider) {
        return provider == InstagramProvider.FAKE;
    }

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
