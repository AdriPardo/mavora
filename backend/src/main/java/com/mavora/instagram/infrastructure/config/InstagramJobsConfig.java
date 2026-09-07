package com.mavora.instagram.infrastructure.config;

import com.mavora.instagram.application.InstagramPublishService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class InstagramJobsConfig {

    private final InstagramPublishService publishService;

    public InstagramJobsConfig(InstagramPublishService publishService) {
        this.publishService = publishService;
    }

    @Scheduled(fixedDelayString = "${mavora.instagram.publish-interval-ms:15000}")
    public void publishDue() {
        publishService.processDue();
    }
}
