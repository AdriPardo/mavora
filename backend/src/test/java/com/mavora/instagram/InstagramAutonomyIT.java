package com.mavora.instagram;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.support.PostgresTestSupport;
import com.mavora.support.RegisteredOrg;
import java.util.Base64;
import java.util.UUID;
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
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InstagramAutonomyIT {

    private static final byte[] JPEG = Base64.getDecoder().decode(
            "/9j/4AAQSkZJRgABAQAAAQABAAD/2wAAAQIBAQEBAQIBAQECAgICAgQDAwIDBQQFBQUFBwcGBwcHBwcI"
                    + "CAgICAgKCgoKCgoLCwsLCw8PDw8PDw8PDw8P/8AAEQgAAQABAwERAAIRAQMRAf/EABQAAQAAAAAAAAAA"
                    + "AAAAAAAAAAj/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIQAxAAAAGf/8QAFBABAAAAAAAAAAAA"
                    + "AAAAAAAAAP/aAAgBAQABPwB//9k="
    );

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
        registry.add("mavora.media.directory", () -> "build/tmp/mavora-media-" + UUID.randomUUID());
    }

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void connectsPlansAndPublishesWithoutApproval() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Ig Labs");
        RegisteredOrg other = RegisteredOrg.register(client, objectMapper, "Other Ig");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        assertThat(client.get().uri(base + "/instagram").header(HttpHeaders.COOKIE, cookie)
                .retrieve().toEntity(String.class).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> weekWithoutSetup = client.post()
                .uri(base + "/instagram/week")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(weekWithoutSetup.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        putCompany(client, cookie, base);
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
        assertThat(status.path("autonomyEnabled").asBoolean()).isTrue();
        assertThat(status.path("username").asText()).isEqualTo("acme.demo");
        assertThat(connected.getBody()).doesNotContain("token");
        assertThat(connected.getBody()).doesNotContain("cipher");

        ResponseEntity<String> brief = client.put()
                .uri(base + "/instagram/brief")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "voice":"Directa y clara",
                          "offer":"Analítica para micro-SaaS",
                          "cta":"Escríbenos DEMO por DM",
                          "audience":"Founders en España",
                          "extraNotes":"Foto del dashboard en claro"
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(brief.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> uploaded = uploadJpeg(client, cookie, base, "dashboard.jpg");
        assertThat(uploaded.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(uploaded.getBody()).contains("dashboard.jpg");

        ResponseEntity<String> week = client.post()
                .uri(base + "/instagram/week")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(week.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        ResponseEntity<String> slotsResponse = client.get()
                .uri(base + "/instagram/slots")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(slotsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode slots = objectMapper.readTree(slotsResponse.getBody()).path("items");
        assertThat(slots.size()).isGreaterThanOrEqualTo(8);
        assertThat(slots.toString()).contains("REEL");
        assertThat(slots.toString()).contains("STORY");
        assertThat(slots.toString()).contains("FEED");
        assertThat(slots.toString()).contains("CAROUSEL");
        long published = 0;
        long scheduled = 0;
        for (JsonNode slot : slots) {
            String slotStatus = slot.path("status").asText();
            if ("PUBLISHED".equals(slotStatus)) {
                published++;
                assertThat(slot.path("igMediaId").asText()).startsWith("ig_fake_");
            }
            if ("SCHEDULED".equals(slotStatus)) {
                scheduled++;
            }
        }
        assertThat(published).isGreaterThanOrEqualTo(1);
        assertThat(scheduled).isGreaterThanOrEqualTo(1);

        ResponseEntity<String> media = client.get()
                .uri(base + "/media")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(media.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(media.getBody()).contains("fal-");

        JsonNode playbook = objectMapper.readTree(client.get()
                .uri(base + "/instagram/playbook")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .body(String.class));
        assertThat(playbook.path("llmProvider").asText()).isEqualTo("fake");
        assertThat(playbook.path("mediaProvider").asText()).isEqualTo("fake");

        ResponseEntity<String> approvals = client.get()
                .uri(base + "/approvals")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(approvals.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(approvals.getBody()).doesNotContain("INSTAGRAM");

        ResponseEntity<String> foreign = client.get()
                .uri("/api/v1/organizations/" + other.organizationId + "/instagram/slots")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(foreign.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(foreign.getBody()).doesNotContain("ig_fake_");
    }

    @Test
    void generatesCopyAndScheduleWithoutInstagramConnection() throws Exception {
        RestClient client = RegisteredOrg.restClient(port);
        RegisteredOrg org = RegisteredOrg.register(client, objectMapper, "Manual Ig");
        String cookie = org.cookie;
        String base = "/api/v1/organizations/" + org.organizationId;

        putCompany(client, cookie, base);
        ResponseEntity<String> status = client.get()
                .uri(base + "/instagram")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(objectMapper.readTree(status.getBody()).path("connected").asBoolean()).isFalse();

        ResponseEntity<String> week = client.post()
                .uri(base + "/instagram/week")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(week.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(week.getBody()).doesNotContain("token");
        assertThat(week.getBody()).doesNotContain("cipher");

        JsonNode workflow = objectMapper.readTree(client.get()
                .uri(base + "/workflows/" + objectMapper.readTree(week.getBody()).path("id").asText())
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .body(String.class));
        assertThat(workflow.path("status").asText()).isEqualTo("SUCCEEDED");
        assertThat(workflow.path("errorMessage").asText(null)).isNull();

        ResponseEntity<String> slotsResponse = client.get()
                .uri(base + "/instagram/slots")
                .header(HttpHeaders.COOKIE, cookie)
                .retrieve()
                .toEntity(String.class);
        assertThat(slotsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(slotsResponse.getBody()).doesNotContain("token");
        JsonNode slots = objectMapper.readTree(slotsResponse.getBody()).path("items");
        assertThat(slots.size()).isGreaterThanOrEqualTo(8);
        assertThat(slots.toString()).contains("REEL");
        assertThat(slots.toString()).contains("STORY");
        assertThat(slots.toString()).contains("FEED");
        assertThat(slots.toString()).contains("CAROUSEL");
        for (JsonNode slot : slots) {
            assertThat(slot.path("status").asText()).isEqualTo("SCHEDULED");
            assertThat(slot.path("hook").asText()).isNotBlank();
            assertThat(slot.path("caption").asText()).isNotBlank();
            assertThat(slot.path("cta").asText()).isNotBlank();
            assertThat(slot.path("scheduledAt").asText()).isNotBlank();
            assertThat(slot.path("igMediaId").asText(null)).isNull();
        }
    }

    private static void putCompany(RestClient client, String cookie, String base) {
        ResponseEntity<String> created = client.put()
                .uri(base + "/company")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name":"Ig Analytics",
                          "websiteUrl":"https://example.com",
                          "description":"Analítica para micro-SaaS",
                          "market":"España B2B",
                          "products":[{"name":"Pulse","description":"Inbox de KPIs"}]
                        }
                        """)
                .retrieve()
                .toEntity(String.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private static ResponseEntity<String> uploadJpeg(RestClient client, String cookie, String base, String filename) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.IMAGE_JPEG);
        fileHeaders.setContentDispositionFormData("file", filename);
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new HttpEntity<>(JPEG, fileHeaders));
        body.add("captionHint", "Dashboard del producto");
        return client.post()
                .uri(base + "/media")
                .header(HttpHeaders.COOKIE, cookie)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .toEntity(String.class);
    }
}
