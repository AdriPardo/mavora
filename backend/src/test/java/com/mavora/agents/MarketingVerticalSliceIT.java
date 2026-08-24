package com.mavora.agents;

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
class MarketingVerticalSliceIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void cmoResearchContentSocialAndAnalyticsLoop() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Vertical Labs");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        assertThat(putCompany(client, cookie, base).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postGoal(client, cookie, base).getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> cmo = client.post()
                .uri(base + "/workflows/CMO_STRATEGY")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(cmo.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        JsonNode cmoBody = objectMapper.readTree(cmo.getBody());
        assertThat(cmoBody.path("status").asText()).isIn("QUEUED", "RUNNING", "SUCCEEDED");

        ResponseEntity<String> strategy = client.get()
                .uri(base + "/strategy")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(strategy.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(strategy.getBody()).path("status").asText()).isEqualTo("DRAFT");

        String approvalId = pendingApprovalId(client, cookie, base);
        ResponseEntity<String> approved = client.post()
                .uri(base + "/approvals/" + approvalId + "/decide")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"approved\":true}")
                .retrieve()
                .toEntity(String.class);
        assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(objectMapper.readTree(client.get()
                .uri(base + "/strategy")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .body(String.class)).path("status").asText()).isEqualTo("APPROVED");

        ResponseEntity<String> campaigns = client.get()
                .uri(base + "/campaigns")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(campaigns.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(campaigns.getBody()).contains("Plan de canales");

        assertThat(client.post().uri(base + "/workflows/MARKET_RESEARCH").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        ResponseEntity<String> research = client.get().uri(base + "/research").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class);
        assertThat(research.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(research.getBody()).contains("personas");

        assertThat(client.post().uri(base + "/workflows/CONTENT_CYCLE").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        ResponseEntity<String> content = client.get().uri(base + "/content").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class);
        assertThat(content.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(content.getBody()).contains("PENDING_APPROVAL");

        String contentApproval = pendingApprovalId(client, cookie, base);
        assertThat(client.post()
                .uri(base + "/approvals/" + contentApproval + "/decide")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"approved\":true}")
                .retrieve()
                .toEntity(String.class)
                .getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(client.post().uri(base + "/workflows/SOCIAL_PLAN").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        JsonNode publications = objectMapper.readTree(client.get().uri(base + "/publications")
                .header(HttpHeaders.COOKIE, cookie).retrieve().body(String.class));
        assertThat(publications.path("items").size()).isGreaterThan(0);
        String publicationId = publications.path("items").get(0).path("id").asText();
        ResponseEntity<String> published = client.post()
                .uri(base + "/publications/" + publicationId + "/publish")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(published.getBody()).contains("PUBLISHED");

        assertThat(client.post()
                .uri(base + "/analytics/snapshots")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"metric\":\"signups\",\"value\":12,\"source\":\"manual\"}")
                .retrieve()
                .toEntity(String.class)
                .getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(client.post().uri(base + "/workflows/ANALYTICS_CYCLE").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        ResponseEntity<String> knowledge = client.get().uri(base + "/knowledge")
                .header(HttpHeaders.COOKIE, cookie).retrieve().toEntity(String.class);
        assertThat(knowledge.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(knowledge.getBody()).contains("DECISION");
        assertThat(knowledge.getBody()).contains("LEARNING");

        ResponseEntity<String> usage = client.get().uri(base + "/usage")
                .header(HttpHeaders.COOKIE, cookie).retrieve().toEntity(String.class);
        assertThat(usage.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(usage.getBody()).path("spentCentsThisMonth").asLong()).isGreaterThan(0);

        ResponseEntity<String> duplicate = client.post()
                .uri(base + "/workflows/CMO_STRATEGY")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(duplicate.getStatusCode()).isIn(HttpStatus.ACCEPTED, HttpStatus.CONFLICT);

        RegisteredOrg other = RegisteredOrg.register(client, objectMapper, "Other Vertical");
        ResponseEntity<String> isolated = client.get()
                .uri(base + "/strategy")
                .header(HttpHeaders.COOKIE, other.cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(isolated.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String pendingApprovalId(RestClient client, String cookie, String base) throws Exception {
        JsonNode body = objectMapper.readTree(client.get().uri(base + "/approvals")
                .header(HttpHeaders.COOKIE, cookie).retrieve().body(String.class));
        for (JsonNode item : body.path("items")) {
            if ("PENDING".equals(item.path("status").asText())) {
                return item.path("id").asText();
            }
        }
        throw new AssertionError("No pending approval: " + body);
    }

    private static ResponseEntity<String> putCompany(RestClient client, String cookie, String base) {
        return client.put()
                .uri(base + "/company")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Vertical Labs",
                          "description":"Autonomous marketing",
                          "market":"España",
                          "products":[{"name":"Mavora"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
    }

    private static ResponseEntity<String> postGoal(RestClient client, String cookie, String base) {
        return client.post()
                .uri(base + "/goals")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "metric":"signups",
                          "targetValue":50,
                          "deadline":"2026-12-31",
                          "budgetCents":80000,
                          "budgetCurrency":"EUR",
                          "market":"España"
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
    }
}
