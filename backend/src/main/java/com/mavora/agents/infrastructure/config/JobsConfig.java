package com.mavora.agents.infrastructure.config;

import com.mavora.agents.application.WorkflowProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class JobsConfig {

    private final WorkflowProcessor workflowProcessor;

    public JobsConfig(WorkflowProcessor workflowProcessor) {
        this.workflowProcessor = workflowProcessor;
    }

    @Scheduled(fixedDelayString = "${mavora.jobs.poll-interval-ms:2000}")
    public void poll() {
        for (int i = 0; i < 5; i++) {
            if (!workflowProcessor.processNext()) {
                return;
            }
        }
    }
}
