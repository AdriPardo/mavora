package com.mavora.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI mavoraOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mavora API")
                        .version("v1")
                        .description("Autonomous AI Marketing Team"));
    }
}
