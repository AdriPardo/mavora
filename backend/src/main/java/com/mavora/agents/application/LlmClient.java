package com.mavora.agents.application;

public interface LlmClient {

    LlmCompletion complete(LlmRequest request);
}
