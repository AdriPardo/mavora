package com.mavora.instagram.infrastructure.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.instagram.application.InstagramOAuthClient;
import com.mavora.instagram.application.MetaAppCredentials;
import com.mavora.instagram.application.MetaConnectionService;
import com.mavora.shared.domain.DomainException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class MetaInstagramOAuthClient implements InstagramOAuthClient {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MetaInstagramOAuthClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    }

    @Override
    public String authorizeUrl(MetaAppCredentials credentials, String state) {
        requireConfigured(credentials);
        String graphVersion = credentials.graphVersion();
        return "https://www.facebook.com/" + graphVersion + "/dialog/oauth"
                + "?client_id=" + encode(credentials.appId())
                + "&redirect_uri=" + encode(credentials.redirectUri())
                + "&state=" + encode(state)
                + "&scope=" + encode(String.join(",", MetaConnectionService.REQUIRED_SCOPES))
                + "&response_type=code";
    }

    @Override
    public ConnectedAccount exchange(MetaAppCredentials credentials, String code) {
        requireConfigured(credentials);
        String graphVersion = credentials.graphVersion();
        String appId = credentials.appId();
        String appSecret = credentials.appSecret();
        String redirectUri = credentials.redirectUri();
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

    private static void requireConfigured(MetaAppCredentials credentials) {
        if (credentials == null || !credentials.isReady()) {
            throw new DomainException("Meta app id and secret are not configured");
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
