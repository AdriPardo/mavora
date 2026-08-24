package com.mavora.agents.application;

public record HandlerResult(String outputJson, LlmCompletion completion) {

    public static HandlerResult of(String outputJson, LlmCompletion completion) {
        return new HandlerResult(outputJson, completion);
    }
}
