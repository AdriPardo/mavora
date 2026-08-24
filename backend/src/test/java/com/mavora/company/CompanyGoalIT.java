package com.mavora.company;

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
class CompanyGoalIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void upsertsCompanyCreatesGoalRejectsPrivateWebsiteAndIsolatesTenants() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg acme = RegisteredOrg.register(client, objectMapper, "Acme Slice");
        RegisteredOrg other = RegisteredOrg.register(client, objectMapper, "Other Slice");

        ResponseEntity<String> missing = client.get()
                .uri("/api/v1/organizations/" + acme.organizationId + "/company")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> created = client.put()
                .uri("/api/v1/organizations/" + acme.organizationId + "/company")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Acme Analytics",
                          "websiteUrl":"https://example.com",
                          "description":"Analytics for micro-SaaS",
                          "market":"España B2B",
                          "products":[{"name":"Acme Pulse","description":"KPI inbox"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(created.getBody()).contains("Acme Pulse");

        ResponseEntity<String> foreignCompany = client.get()
                .uri("/api/v1/organizations/" + other.organizationId + "/company")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(foreignCompany.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<String> localhostCompany = client.put()
                .uri("/api/v1/organizations/" + acme.organizationId + "/company")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Acme Analytics",
                          "websiteUrl":"http://127.0.0.1/",
                          "description":"Analytics for micro-SaaS",
                          "market":"España B2B",
                          "products":[{"name":"Acme Pulse"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(localhostCompany.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> blockedFetch = client.post()
                .uri("/api/v1/organizations/" + acme.organizationId + "/company/website-ingest")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(blockedFetch.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(blockedFetch.getBody()).contains("not allowed");

        ResponseEntity<String> restored = client.put()
                .uri("/api/v1/organizations/" + acme.organizationId + "/company")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Acme Analytics",
                          "websiteUrl":"https://example.com",
                          "description":"Analytics for micro-SaaS",
                          "market":"España B2B",
                          "products":[{"name":"Acme Pulse","description":"KPI inbox"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(restored.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> goal = client.post()
                .uri("/api/v1/organizations/" + acme.organizationId + "/goals")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "metric":"signups",
                          "targetValue":200,
                          "deadline":"2026-12-31",
                          "budgetCents":150000,
                          "budgetCurrency":"EUR",
                          "market":"España"
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(goal.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode goalBody = objectMapper.readTree(goal.getBody());
        assertThat(goalBody.path("budgetCents").asLong()).isEqualTo(150000);
        assertThat(goalBody.path("budgetCurrency").asText()).isEqualTo("EUR");

        ResponseEntity<String> workspace = client.get()
                .uri("/api/v1/organizations/" + acme.organizationId + "/workspace")
                .header(HttpHeaders.COOKIE, acme.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(workspace.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(workspace.getBody()).contains("Acme Analytics");
        assertThat(workspace.getBody()).contains("signups");
    }
}
