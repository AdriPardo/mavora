package com.mavora.agents.application;

public record LlmCompletion(
        String content,
        String model,
        int promptTokens,
        int completionTokens,
        long costCents
) {
}
