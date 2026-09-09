package com.mavora.instagram.infrastructure.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.instagram.application.InstagramProfileReader;
import com.mavora.instagram.application.InstagramProfileSnapshot;
import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramProvider;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetaInstagramProfileReader implements InstagramProfileReader {

    private static final Logger log = LoggerFactory.getLogger(MetaInstagramProfileReader.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MetaInstagramProfileReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public boolean supports(InstagramProvider provider) {
        return provider == InstagramProvider.META;
    }

    @Override
    public InstagramProfileSnapshot read(InstagramAccount account, String accessToken, String graphVersion) {
        String version = graphVersion == null || graphVersion.isBlank() ? "v21.0" : graphVersion.trim();
        String token = accessToken == null ? "" : accessToken;
        try {
            JsonNode ig = get(version, "/" + account.igUserId(),
                    "username,name,biography,website,followers_count,media_count", token);
            JsonNode media = get(version, "/" + account.igUserId() + "/media", "caption", token);
            JsonNode page = account.pageId() == null || account.pageId().isBlank()
                    ? null
                    : get(version, "/" + account.pageId(), "name,about,website,category", token);
            List<String> captions = new ArrayList<>();
            for (JsonNode item : media.path("data")) {
                String caption = item.path("caption").asText("");
                if (!caption.isBlank()) {
                    captions.add(caption.length() > 280 ? caption.substring(0, 280) : caption);
                }
                if (captions.size() >= 8) {
                    break;
                }
            }
            String website = text(ig, "website");
            if (website == null && page != null) {
                website = text(page, "website");
            }
            return new InstagramProfileSnapshot(
                    text(ig, "username") != null ? text(ig, "username") : account.username(),
                    text(ig, "name"),
                    text(ig, "biography"),
                    website,
                    ig.path("followers_count").isNumber() ? ig.path("followers_count").asInt() : null,
                    ig.path("media_count").isNumber() ? ig.path("media_count").asInt() : null,
                    page == null ? null : text(page, "name"),
                    page == null ? null : text(page, "category"),
                    page == null ? null : text(page, "about"),
                    captions
            );
        } catch (Exception exception) {
            log.info("Could not read Instagram Graph profile, using handle only: {}", exception.getMessage());
            return new InstagramProfileSnapshot(
                    account.username(), null, null, null, null, null, null, null, null, List.of()
            );
        }
    }

    private JsonNode get(String version, String path, String fields, String token) throws Exception {
        String url = "https://graph.facebook.com/" + version + path
                + "?fields=" + encode(fields)
                + "&access_token=" + encode(token)
                + "&limit=8";
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode node = objectMapper.readTree(response.body() == null ? "{}" : response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || node.has("error")) {
            throw new IllegalStateException(node.path("error").path("message").asText("Graph profile error"));
        }
        return node;
    }

    private static String text(JsonNode node, String field) {
        String value = node.path(field).asText("");
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
