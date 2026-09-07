package com.mavora.instagram.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.instagram.application.InstagramOAuthClient;
import com.mavora.shared.domain.DomainException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "mavora.instagram.provider", havingValue = "meta")
public class MetaInstagramOAuthClient implements InstagramOAuthClient {

    private static final String SCOPES = String.join(",", List.of(
            "instagram_basic",
            "instagram_content_publish",
            "instagram_manage_insights",
            "pages_show_list",
            "pages_read_engagement"
    ));

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String appId;
    private final String appSecret;
    private final String redirectUri;
    private final String graphVersion;

    public MetaInstagramOAuthClient(
            ObjectMapper objectMapper,
            @Value("${mavora.instagram.app-id:}") String appId,
            @Value("${mavora.instagram.app-secret:}") String appSecret,
            @Value("${mavora.instagram.redirect-uri:http://localhost:8080/api/v1/integrations/instagram/callback}") String redirectUri,
            @Value("${mavora.instagram.graph-version:v21.0}") String graphVersion
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        this.appId = appId;
        this.appSecret = appSecret;
        this.redirectUri = redirectUri;
        this.graphVersion = graphVersion;
    }

    @Override
    public String authorizeUrl(String state) {
        requireConfigured();
        return "https://www.facebook.com/" + graphVersion + "/dialog/oauth"
                + "?client_id=" + encode(appId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(state)
                + "&scope=" + encode(SCOPES)
                + "&response_type=code";
    }

    @Override
    public ConnectedAccount exchange(String code) {
        requireConfigured();
        try {
            JsonNode shortLived = get("https://graph.facebook.com/" + graphVersion + "/oauth/access_token"
                    + "?client_id=" + encode(appId)
                    + "&client_secret=" + encode(appSecret)
                    + "&redirect_uri=" + encode(redirectUri)
                    + "&code=" + encode(code));
            String userToken = shortLived.path("access_token").asText();
            JsonNode longLived = get("https://graph.facebook.com/" + graphVersion + "/oauth/access_token"
                    + "?grant_type=fb_exchange_token"
                    + "&client_id=" + encode(appId)
                    + "&client_secret=" + encode(appSecret)
                    + "&fb_exchange_token=" + encode(userToken));
            String token = longLived.path("access_token").asText(userToken);
            long expiresIn = longLived.path("expires_in").asLong(60L * 60 * 24 * 60);
            JsonNode pages = get("https://graph.facebook.com/" + graphVersion + "/me/accounts?access_token=" + encode(token));
            for (JsonNode page : pages.path("data")) {
                String pageId = page.path("id").asText();
                String pageToken = page.path("access_token").asText(token);
                JsonNode ig = get("https://graph.facebook.com/" + graphVersion + "/" + pageId
                        + "?fields=instagram_business_account{id,username}&access_token=" + encode(pageToken));
                JsonNode account = ig.path("instagram_business_account");
                if (account.path("id").asText("").isBlank()) {
                    continue;
                }
                return new ConnectedAccount(
                        account.path("id").asText(),
                        account.path("username").asText("unknown"),
                        pageId,
                        pageToken,
                        Instant.now().plusSeconds(expiresIn)
                );
            }
            throw new DomainException("No Instagram professional account is linked to a Facebook Page");
        } catch (DomainException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new DomainException("Instagram OAuth exchange failed");
        }
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonNode node = objectMapper.readTree(response.body() == null ? "{}" : response.body());
        if (response.statusCode() < 200 || response.statusCode() >= 300 || node.has("error")) {
            throw new DomainException(node.path("error").path("message").asText("Instagram OAuth error"));
        }
        return node;
    }

    private void requireConfigured() {
        if (appId == null || appId.isBlank() || appSecret == null || appSecret.isBlank()) {
            throw new DomainException("Meta app id and secret are not configured");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
