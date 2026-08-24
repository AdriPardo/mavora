package com.mavora.organization;

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
class TenantIsolationIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void memberCannotReadAnotherOrganization() throws Exception {
        RestClient client = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                })
                .build();

        RegisteredUser alice = register(client, "Alice Labs");
        RegisteredUser bob = register(client, "Bob Industries");

        ResponseEntity<String> own = client.get()
                .uri("/api/v1/organizations/" + alice.organizationId)
                .header(HttpHeaders.COOKIE, alice.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(own.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(own.getBody()).contains("Alice Labs");

        ResponseEntity<String> foreign = client.get()
                .uri("/api/v1/organizations/" + bob.organizationId)
                .header(HttpHeaders.COOKIE, alice.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(foreign.getBody()).contains("Organization not found");
        assertThat(foreign.getBody()).doesNotContain("Bob Industries");

        ResponseEntity<String> foreignMembers = client.get()
                .uri("/api/v1/organizations/" + bob.organizationId + "/members")
                .header(HttpHeaders.COOKIE, alice.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(foreignMembers.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> anonymous = client.get()
                .uri("/api/v1/organizations/" + alice.organizationId)
                .retrieve()
                .toEntity(String.class);
        assertThat(anonymous.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private RegisteredUser register(RestClient client, String organizationName) throws Exception {
        String email = organizationName.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID() + "@example.com";
        ResponseEntity<String> response = client.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"email":"%s","password":"super-secret-1","organizationName":"%s"}
                        """.formatted(email, organizationName))
                .retrieve()
                .toEntity(String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = objectMapper.readTree(response.getBody());
        return new RegisteredUser(
                email,
                body.path("organizations").get(0).path("id").asText(),
                SessionCookieSupport.cookieHeader(response.getHeaders())
        );
    }

    private record RegisteredUser(String email, String organizationId, String cookie) {
    }
}
