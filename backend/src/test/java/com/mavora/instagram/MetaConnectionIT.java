package com.mavora.instagram;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.support.PostgresTestSupport;
import com.mavora.support.RegisteredOrg;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MetaConnectionIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void savesMetaKeysWithoutLeakingSecretAndBuildsOAuthUrl() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Meta Labs");
        RegisteredOrg other = RegisteredOrg.register(client, objectMapper, "Other Meta");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        ResponseEntity<String> empty = client.get()
                .uri(base + "/instagram/meta")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode setup = objectMapper.readTree(empty.getBody());
        assertThat(setup.path("oauthReady").asBoolean()).isFalse();
        assertThat(setup.path("secretConfigured").asBoolean()).isFalse();
        assertThat(setup.path("suggestedRedirectUri").asText()).contains("/api/v1/integrations/instagram/callback");
        assertThat(setup.path("privacyPolicyUrl").asText()).endsWith("/privacidad");
        assertThat(setup.path("appIconUrl").asText()).endsWith("/meta-app-icon.png");
        assertThat(setup.path("scopes").isArray()).isTrue();
        assertThat(setup.path("scopes").toString())
                .contains("instagram_basic", "instagram_content_publish", "pages_show_list", "pages_read_engagement")
                .doesNotContain("instagram_manage_insights")
                .doesNotContain("ads_management")
                .doesNotContain("ads_read");
        assertThat(empty.getBody()).doesNotContain("appSecret");
        assertThat(empty.getBody()).doesNotContain("ciphertext");

        ResponseEntity<String> missingSecret = client.put()
                .uri(base + "/instagram/meta")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"appId\":\"987654321098765\",\"graphVersion\":\"v21.0\"}")
                .retrieve()
                .toEntity(String.class);
        assertThat(missingSecret.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<String> saved = client.put()
                .uri(base + "/instagram/meta")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"appId\":\"987654321098765\",\"appSecret\":\"meta-app-secret-value\",\"graphVersion\":\"v21.0\"}")
                .retrieve()
                .toEntity(String.class);
        assertThat(saved.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode stored = objectMapper.readTree(saved.getBody());
        assertThat(stored.path("appId").asText()).isEqualTo("987654321098765");
        assertThat(stored.path("secretConfigured").asBoolean()).isTrue();
        assertThat(stored.path("oauthReady").asBoolean()).isTrue();
        assertThat(stored.path("source").asText()).isEqualTo("organization");
        assertThat(saved.getBody()).doesNotContain("meta-app-secret-value");
        assertThat(saved.getBody()).doesNotContain("appSecret");

        ResponseEntity<String> updated = client.put()
                .uri(base + "/instagram/meta")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"appId\":\"111222333444555\",\"graphVersion\":\"v22.0\"}")
                .retrieve()
                .toEntity(String.class);
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode afterUpdate = objectMapper.readTree(updated.getBody());
        assertThat(afterUpdate.path("appId").asText()).isEqualTo("111222333444555");
        assertThat(afterUpdate.path("graphVersion").asText()).isEqualTo("v22.0");
        assertThat(afterUpdate.path("secretConfigured").asBoolean()).isTrue();
        assertThat(updated.getBody()).doesNotContain("meta-app-secret-value");

        ResponseEntity<String> connectUrl = client.get()
                .uri(base + "/instagram/connect-url")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(connectUrl.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode urlBody = objectMapper.readTree(connectUrl.getBody());
        String url = urlBody.path("url").asText();
        assertThat(url).startsWith("https://www.facebook.com/v22.0/dialog/oauth");
        assertThat(URLDecoder.decode(url, StandardCharsets.UTF_8)).contains("client_id=111222333444555");
        String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
        assertThat(decoded).contains("instagram_content_publish");
        assertThat(decoded).contains("instagram_basic");
        assertThat(decoded).contains("pages_show_list");
        assertThat(decoded).contains("pages_read_engagement");
        assertThat(decoded).doesNotContain("instagram_manage_insights");
        assertThat(decoded).doesNotContain("ads_management");
        assertThat(connectUrl.getBody()).doesNotContain("meta-app-secret-value");

        ResponseEntity<String> token = client.post()
                .uri(base + "/instagram/connect-token")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "username":"marca.real",
                          "igUserId":"17841400000000000",
                          "pageId":"1234567890",
                          "accessToken":"EAAG-this-is-a-long-lived-page-token"
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(token.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode connected = objectMapper.readTree(token.getBody());
        assertThat(connected.path("connected").asBoolean()).isTrue();
        assertThat(connected.path("provider").asText()).isEqualTo("meta");
        assertThat(connected.path("username").asText()).isEqualTo("marca.real");
        assertThat(token.getBody()).doesNotContain("EAAG");
        assertThat(token.getBody()).doesNotContain("accessToken");

        ResponseEntity<String> foreign = client.get()
                .uri("/api/v1/organizations/" + other.organizationId + "/instagram/meta")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(foreign.getBody()).doesNotContain("111222333444555");
    }

    @Test
    void connectUrlWithoutKeysExplainsWhatIsMissing() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "No Keys");
        ResponseEntity<String> response = client.get()
                .uri("/api/v1/organizations/" + org.organizationId + "/instagram/connect-url")
                .header(HttpHeaders.COOKIE, org.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("App ID");
    }
}
