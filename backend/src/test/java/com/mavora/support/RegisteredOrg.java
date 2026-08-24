package com.mavora.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public final class RegisteredOrg {

    public final String email;
    public final String organizationId;
    public final String cookie;

    public RegisteredOrg(String email, String organizationId, String cookie) {
        this.email = email;
        this.organizationId = organizationId;
        this.cookie = cookie;
    }

    public static RegisteredOrg register(RestClient client, ObjectMapper objectMapper, String organizationName)
            throws Exception {
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
        return new RegisteredOrg(
                email,
                body.path("organizations").get(0).path("id").asText(),
                SessionCookieSupport.cookieHeader(response.getHeaders())
        );
    }

    public static RestClient restClient(int port) {
        return RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port)
                .defaultStatusHandler(org.springframework.http.HttpStatusCode::isError, (request, response) -> {
                })
                .build();
    }
}
