package com.mavora.instagram.infrastructure.publish;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.instagram.application.InstagramPublisher;
import com.mavora.instagram.domain.InstagramFormat;
import com.mavora.shared.domain.DomainException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.mavora.instagram.domain.InstagramProvider;
import org.springframework.stereotype.Component;

@Component
public class MetaInstagramPublisher implements InstagramPublisher {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MetaInstagramPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public boolean supports(InstagramProvider provider) {
        return provider == InstagramProvider.META;
    }

    @Override
    public PublishResult publish(PublishCommand command) {
        try {
            String creationId = switch (command.format()) {
                case STORY -> createStory(command);
                case REEL -> createReel(command);
                case CAROUSEL -> createCarousel(command);
                case FEED -> createFeedImage(command);
            };
            waitUntilFinished(creationId, command.accessToken(), command.graphVersion());
            JsonNode published = post(
                    "/" + command.igUserId() + "/media_publish",
                    command.accessToken(),
                    Map.of("creation_id", creationId),
                    command.graphVersion()
            );
            String id = published.path("id").asText();
            if (id.isBlank()) {
                throw new DomainException("Instagram did not return a media id");
            }
            return new PublishResult(id);
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Instagram publish failed");
        }
    }

    private String createFeedImage(PublishCommand command) throws Exception {
        if (command.imageUrls().isEmpty()) {
            throw new DomainException("A JPEG image is required for a feed post");
        }
        JsonNode node = post("/" + command.igUserId() + "/media", command.accessToken(), Map.of(
                "image_url", command.imageUrls().get(0),
                "caption", command.caption()
        ), command.graphVersion());
        return requireId(node);
    }

    private String createReel(PublishCommand command) throws Exception {
        if (command.videoUrl() == null || command.videoUrl().isBlank()) {
            throw new DomainException("A video is required for a Reel. Upload an MP4 or Mavora will skip Reels.");
        }
        JsonNode node = post("/" + command.igUserId() + "/media", command.accessToken(), Map.of(
                "media_type", "REELS",
                "video_url", command.videoUrl(),
                "caption", command.caption(),
                "share_to_feed", "true"
        ), command.graphVersion());
        return requireId(node);
    }

    private String createStory(PublishCommand command) throws Exception {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("media_type", "STORIES");
        if (command.videoUrl() != null && !command.videoUrl().isBlank()) {
            fields.put("video_url", command.videoUrl());
        } else if (!command.imageUrls().isEmpty()) {
            fields.put("image_url", command.imageUrls().get(0));
        } else {
            throw new DomainException("A photo or video is required for a story");
        }
        return requireId(post("/" + command.igUserId() + "/media", command.accessToken(), fields, command.graphVersion()));
    }

    private String createCarousel(PublishCommand command) throws Exception {
        List<String> urls = command.imageUrls();
        if (urls.size() < 2) {
            return createFeedImage(command);
        }
        List<String> children = new ArrayList<>();
        for (String url : urls.subList(0, Math.min(10, urls.size()))) {
            JsonNode child = post("/" + command.igUserId() + "/media", command.accessToken(), Map.of(
                    "image_url", url,
                    "is_carousel_item", "true"
            ), command.graphVersion());
            children.add(requireId(child));
        }
        JsonNode parent = post("/" + command.igUserId() + "/media", command.accessToken(), Map.of(
                "media_type", "CAROUSEL",
                "children", String.join(",", children),
                "caption", command.caption()
        ), command.graphVersion());
        return requireId(parent);
    }

    private void waitUntilFinished(String creationId, String accessToken, String graphVersion) throws Exception {
        for (int i = 0; i < 20; i++) {
            JsonNode status = get("/" + creationId, accessToken, "status_code,status", graphVersion);
            String code = status.path("status_code").asText("");
            if ("FINISHED".equalsIgnoreCase(code) || code.isBlank()) {
                return;
            }
            if ("ERROR".equalsIgnoreCase(code) || "EXPIRED".equalsIgnoreCase(code)) {
                throw new DomainException("Instagram container status: " + code);
            }
            Thread.sleep(2000);
        }
        throw new DomainException("Instagram container did not finish in time");
    }

    private JsonNode post(String path, String accessToken, Map<String, String> fields, String graphVersion)
            throws Exception {
        String body = fields.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));
        body = body + "&access_token=" + encode(accessToken);
        HttpRequest request = HttpRequest.newBuilder(URI.create(graphBase(graphVersion) + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return send(request);
    }

    private JsonNode get(String path, String accessToken, String fields, String graphVersion) throws Exception {
        String url = graphBase(graphVersion) + path + "?fields=" + encode(fields)
                + "&access_token=" + encode(accessToken);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return send(request);
    }

    private JsonNode send(HttpRequest request) throws Exception {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode node = objectMapper.readTree(response.body() == null ? "{}" : response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || node.has("error")) {
            String message = node.path("error").path("message").asText("Instagram Graph API error");
            throw new DomainException(message);
        }
        return node;
    }

    private static String requireId(JsonNode node) {
        String id = node.path("id").asText();
        if (id == null || id.isBlank()) {
            throw new DomainException("Instagram did not return a container id");
        }
        return id;
    }

    private static String graphBase(String graphVersion) {
        String version = graphVersion == null || graphVersion.isBlank() ? "v21.0" : graphVersion.trim();
        return "https://graph.facebook.com/" + version;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
