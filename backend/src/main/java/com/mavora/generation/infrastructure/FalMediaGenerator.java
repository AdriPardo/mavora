package com.mavora.generation.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mavora.generation.application.MediaGenerator;
import com.mavora.instagram.domain.MediaKind;
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
@ConditionalOnProperty(name = "mavora.fal.provider", havingValue = "fal")
public class FalMediaGenerator implements MediaGenerator {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String imageModel;
    private final String videoModel;
    private final Duration timeout;

    public FalMediaGenerator(
            ObjectMapper objectMapper,
            @Value("${mavora.fal.api-key:}") String apiKey,
            @Value("${mavora.fal.image-model:fal-ai/flux/schnell}") String imageModel,
            @Value("${mavora.fal.video-model:fal-ai/wan-25-preview/text-to-video}") String videoModel,
            @Value("${mavora.fal.timeout:180s}") Duration timeout
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.imageModel = imageModel;
        this.videoModel = videoModel;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public GeneratedMedia generate(GenerateCommand command) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new DomainException("FAL_KEY is not configured");
        }
        try {
            boolean video = command.kind() == MediaKind.VIDEO;
            String model = video ? videoModel : imageModel;
            ObjectNode input = objectMapper.createObjectNode();
            String prompt = buildPrompt(command);
            input.put("prompt", prompt);
            if (video) {
                input.put("aspect_ratio", aspect(command.aspectRatio(), "9:16"));
                input.put("duration", "5");
                input.put("resolution", "480p");
            } else {
                input.put("image_size", imageSize(command.aspectRatio()));
                input.put("num_images", 1);
                input.put("output_format", "jpeg");
            }
            JsonNode queued = postJson("https://queue.fal.run/" + model, input);
            String requestId = queued.path("request_id").asText();
            if (requestId.isBlank()) {
                throw new DomainException("fal.ai did not accept the generation request");
            }
            String statusUrl = queued.path("status_url").asText(
                    "https://queue.fal.run/" + model + "/requests/" + requestId + "/status"
            );
            String responseUrl = queued.path("response_url").asText(
                    "https://queue.fal.run/" + model + "/requests/" + requestId
            );
            waitCompleted(statusUrl);
            JsonNode result = getJson(responseUrl);
            String fileUrl = extractFileUrl(result);
            byte[] bytes = download(fileUrl);
            if (bytes.length == 0) {
                throw new DomainException("fal.ai returned an empty file");
            }
            if (video) {
                return new GeneratedMedia(MediaKind.VIDEO, "video/mp4", "fal-reel.mp4", bytes, prompt);
            }
            return new GeneratedMedia(MediaKind.IMAGE, "image/jpeg", "fal-image.jpg", bytes, prompt);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("fal.ai generation failed");
        }
    }

    private void waitCompleted(String statusUrl) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            JsonNode status = getJson(statusUrl);
            String value = status.path("status").asText("");
            if ("COMPLETED".equalsIgnoreCase(value)) {
                return;
            }
            if ("FAILED".equalsIgnoreCase(value) || "ERROR".equalsIgnoreCase(value)) {
                throw new DomainException("fal.ai generation status: " + value);
            }
            Thread.sleep(1500);
        }
        throw new DomainException("fal.ai generation timed out");
    }

    static String extractFileUrl(JsonNode result) {
        if (result.hasNonNull("images") && result.path("images").isArray() && result.path("images").size() > 0) {
            return result.path("images").path(0).path("url").asText();
        }
        if (result.path("image").hasNonNull("url")) {
            return result.path("image").path("url").asText();
        }
        if (result.path("video").hasNonNull("url")) {
            return result.path("video").path("url").asText();
        }
        if (result.hasNonNull("url")) {
            return result.path("url").asText();
        }
        throw new DomainException("fal.ai did not return a file URL");
    }

    private JsonNode postJson(String url, ObjectNode body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Key " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        return readJson(request);
    }

    private JsonNode getJson(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Key " + apiKey)
                .GET()
                .build();
        return readJson(request);
    }

    private JsonNode readJson(HttpRequest request) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new DomainException("fal.ai returned HTTP " + response.statusCode());
        }
        return objectMapper.readTree(response.body() == null ? "{}" : response.body());
    }

    private byte[] download(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new DomainException("Could not download fal.ai media");
        }
        return response.body() == null ? new byte[0] : response.body();
    }

    private static String buildPrompt(GenerateCommand command) {
        String prompt = command.prompt() == null ? "" : command.prompt().trim();
        if (command.brandContext() != null && !command.brandContext().isBlank()) {
            prompt = prompt + " Brand context: " + command.brandContext().trim();
        }
        if (prompt.isBlank()) {
            prompt = "Clean Instagram marketing visual, no watermark, no unreadable text.";
        }
        return prompt;
    }

    private static String imageSize(String aspectRatio) {
        if ("9:16".equals(aspectRatio)) {
            return "portrait_16_9";
        }
        if ("1:1".equals(aspectRatio)) {
            return "square_hd";
        }
        return "portrait_4_3";
    }

    private static String aspect(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
