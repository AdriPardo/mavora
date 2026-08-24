package com.mavora.agents.infrastructure.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mavora.agents.application.LlmClient;
import com.mavora.agents.application.LlmCompletion;
import com.mavora.agents.application.LlmRequest;
import com.mavora.shared.domain.DomainException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mavora.llm.provider", havingValue = "openai")
public class OpenAiCompatibleLlmProvider implements LlmClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    private final Duration timeout;

    public OpenAiCompatibleLlmProvider(
            ObjectMapper objectMapper,
            @Value("${mavora.llm.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${mavora.llm.api-key:}") String apiKey,
            @Value("${mavora.llm.timeout:30s}") Duration timeout
    ) {
        this.objectMapper = objectMapper;
        this.baseUrl = trimSlash(baseUrl);
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public LlmCompletion complete(LlmRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new DomainException("LLM API key is not configured");
        }
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", request.model());
            body.put("temperature", 0.2);
            var messages = objectMapper.createArrayNode();
            messages.add(objectMapper.createObjectNode().put("role", "system").put("content", request.systemPrompt()));
            messages.add(objectMapper.createObjectNode().put("role", "user").put("content", request.userPrompt()));
            body.set("messages", messages);
            body.set("response_format", objectMapper.createObjectNode().put("type", "json_object"));

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(baseUrl + "/chat/completions"))
                    .timeout(timeout)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new DomainException("LLM provider returned HTTP " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new DomainException("LLM provider returned an empty completion");
            }
            int promptTokens = root.path("usage").path("prompt_tokens").asInt(0);
            int completionTokens = root.path("usage").path("completion_tokens").asInt(0);
            long costCents = Math.max(1, Math.round((promptTokens + completionTokens) / 4000.0));
            return new LlmCompletion(stripFences(content), request.model(), promptTokens, completionTokens, costCents);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("LLM provider call failed");
        }
    }

    static String stripFences(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNl = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                return trimmed.substring(firstNl + 1, lastFence).trim();
            }
        }
        return trimmed;
    }

    private static String trimSlash(String baseUrl) {
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
