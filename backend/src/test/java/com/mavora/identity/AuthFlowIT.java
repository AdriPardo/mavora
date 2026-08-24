package com.mavora.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.support.PostgresTestSupport;
import com.mavora.support.SessionCookieSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthFlowIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void registerLoginMeLogoutAndRejectsDuplicates() throws Exception {
        RestClient client = restClient();
        String email = "founder-" + UUID.randomUUID() + "@example.com";
        String password = "super-secret-1";
        String body = """
                {"email":"%s","password":"%s","organizationName":"Acme Analytics"}
                """.formatted(email, password);

        ResponseEntity<String> registered = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.OK);
        String setCookie = registered.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("SameSite=Lax");
        assertThat(setCookie).contains("mavora_session=");
        assertThat(setCookie).doesNotContain("password");

        JsonNode account = objectMapper.readTree(registered.getBody());
        assertThat(account.path("user").path("email").asText()).isEqualTo(email);
        assertThat(account.path("user").path("passwordHash").isMissingNode()).isTrue();
        assertThat(account.path("organizations").get(0).path("role").asText()).isEqualTo("OWNER");
        String organizationId = account.path("organizations").get(0).path("id").asText();

        ResponseEntity<String> me = client.get()
                .uri("/api/v1/auth/me")
                .header(HttpHeaders.COOKIE, SessionCookieSupport.cookieHeader(registered.getHeaders()))
                .retrieve()
                .toEntity(String.class);
        assertThat(me.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(me.getBody()).contains(email);

        ResponseEntity<String> duplicate = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(String.class);
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicate.getBody()).doesNotContain("stack");

        ResponseEntity<Void> loggedOut = client.post()
                .uri("/api/v1/auth/logout")
                .header(HttpHeaders.COOKIE, SessionCookieSupport.cookieHeader(registered.getHeaders()))
                .retrieve()
                .toBodilessEntity();
        assertThat(loggedOut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> meAfterLogout = client.get()
                .uri("/api/v1/auth/me")
                .header(HttpHeaders.COOKIE, SessionCookieSupport.cookieHeader(registered.getHeaders()))
                .retrieve()
                .toEntity(String.class);
        assertThat(meAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> login = client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password))
                .retrieve()
                .toEntity(String.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("HttpOnly");

        ResponseEntity<String> members = client.get()
                .uri("/api/v1/organizations/" + organizationId + "/members")
                .header(HttpHeaders.COOKIE, SessionCookieSupport.cookieHeader(login.getHeaders()))
                .retrieve()
                .toEntity(String.class);
        assertThat(members.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(members.getBody()).contains(email);

        ResponseEntity<String> badLogin = client.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"email":"%s","password":"wrong-password"}
                        """.formatted(email))
                .retrieve()
                .toEntity(String.class);
        assertThat(badLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(badLogin.getBody()).contains("Invalid email or password");
        assertThat(badLogin.getBody()).doesNotContain("password_hash");
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
