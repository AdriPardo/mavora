package com.mavora.instagram;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.support.PostgresTestSupport;
import com.mavora.support.RegisteredOrg;
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
class InstagramBusinessImportIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void connectFakeFillsEmptyBusinessFieldsFromProfile() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Import Labs");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        ResponseEntity<String> connected = client.post()
                .uri(base + "/instagram/connect-fake")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"username\":\"acme.demo\"}")
                .retrieve()
                .toEntity(String.class);
        assertThat(connected.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode status = objectMapper.readTree(connected.getBody());
        assertThat(status.path("connected").asBoolean()).isTrue();
        assertThat(status.path("filledFromProfile").isArray()).isTrue();
        assertThat(status.path("filledFromProfile").toString()).contains("company.name");
        assertThat(status.path("filledFromProfile").toString()).contains("brief.offer");
        assertThat(status.path("profileSummary").asText()).contains("acme.demo");
        assertThat(connected.getBody()).doesNotContain("token");

        ResponseEntity<String> company = client.get()
                .uri(base + "/company")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(company.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode profile = objectMapper.readTree(company.getBody());
        assertThat(profile.path("name").asText()).isEqualTo("Acme Demo");
        assertThat(profile.path("websiteUrl").asText()).contains("example.com");
        assertThat(profile.path("description").asText()).isNotBlank();
        assertThat(profile.path("products").isArray()).isTrue();
        assertThat(profile.path("products").size()).isGreaterThanOrEqualTo(1);

        ResponseEntity<String> brief = client.get()
                .uri(base + "/instagram/brief")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(brief.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode briefBody = objectMapper.readTree(brief.getBody());
        assertThat(briefBody.path("offer").asText()).isNotBlank();
        assertThat(briefBody.path("cta").asText()).isNotBlank();
        assertThat(briefBody.path("audience").asText()).isNotBlank();

        ResponseEntity<String> knowledge = client.get()
                .uri(base + "/knowledge")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(knowledge.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(knowledge.getBody()).contains("Instagram");
        assertThat(knowledge.getBody()).contains("acme.demo");
    }

    @Test
    void reconnectDoesNotOverwriteExistingCompany() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Keep Name");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        ResponseEntity<String> created = client.put()
                .uri(base + "/company")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Nombre a mano",
                          "websiteUrl":"https://already.example",
                          "description":"Escrito por el usuario",
                          "market":"España B2B",
                          "products":[{"name":"Producto propio"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        client.post()
                .uri(base + "/instagram/connect-fake")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"username\":\"acme.demo\"}")
                .retrieve()
                .toEntity(String.class);

        JsonNode profile = objectMapper.readTree(client.get()
                .uri(base + "/company")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .body(String.class));
        assertThat(profile.path("name").asText()).isEqualTo("Nombre a mano");
        assertThat(profile.path("websiteUrl").asText()).isEqualTo("https://already.example");
        assertThat(profile.path("description").asText()).isEqualTo("Escrito por el usuario");
        assertThat(profile.path("products").get(0).path("name").asText()).isEqualTo("Producto propio");
    }
}
