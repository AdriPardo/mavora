package com.mavora;

import static org.assertj.core.api.Assertions.assertThat;

import com.mavora.support.PostgresTestSupport;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MavoraApplicationIT {

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        PostgresTestSupport.register(registry);
    }

    @LocalServerPort
    int port;

    @Autowired
    DataSource dataSource;

    @Test
    void contextStartsFlywayRunsAndHealthIsPublic() throws Exception {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select count(*) from flyway_schema_history")) {
            resultSet.next();
            assertThat(resultSet.getInt(1)).isGreaterThan(0);
        }

        RestClient client = RestClient.create();
        String body = client.get()
                .uri("http://127.0.0.1:" + port + "/api/v1/health")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);

        assertThat(body).contains("\"status\":\"ok\"");
        assertThat(body).contains("\"service\":\"mavora\"");
    }

    @Test
    void actuatorHealthDoesNotExposeDetails() {
        RestClient client = RestClient.create();
        String body = client.get()
                .uri("http://127.0.0.1:" + port + "/actuator/health")
                .retrieve()
                .body(String.class);

        assertThat(body).contains("\"status\":\"UP\"");
        assertThat(body).doesNotContain("jdbc");
        assertThat(body).doesNotContain("password");
    }
}
