package com.mavora.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

public final class PostgresTestSupport {

    private static final Logger log = LoggerFactory.getLogger(PostgresTestSupport.class);
    private static PostgreSQLContainer<?> container;

    private PostgresTestSupport() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        if (dockerAvailable()) {
            log.info("Using Testcontainers PostgreSQL");
            container = new PostgreSQLContainer<>("postgres:16-alpine");
            container.start();
            registry.add("spring.datasource.url", container::getJdbcUrl);
            registry.add("spring.datasource.username", container::getUsername);
            registry.add("spring.datasource.password", container::getPassword);
            return;
        }

        log.info("Docker unavailable; using local PostgreSQL");
        registry.add(
                "spring.datasource.url",
                () -> getenvOrDefault("DATABASE_URL", "jdbc:postgresql://localhost:5432/mavora_test")
        );
        registry.add(
                "spring.datasource.username",
                () -> getenvOrDefault("DATABASE_USERNAME", "mavora")
        );
        registry.add(
                "spring.datasource.password",
                () -> getenvOrDefault("DATABASE_PASSWORD", "mavora")
        );
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException exception) {
            log.info("Docker probe failed: {}", exception.getMessage());
            return false;
        }
    }

    private static String getenvOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
