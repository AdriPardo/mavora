package com.mavora.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mavora.shared.infrastructure.config.SecurityConfig;
import com.mavora.shared.infrastructure.web.RequestIdFilter;
import com.mavora.shared.infrastructure.web.RestExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = HealthController.class)
@Import({SecurityConfig.class, RequestIdFilter.class, RestExceptionHandler.class})
class HealthApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthIsPublicAndTyped() throws Exception {
        mockMvc.perform(get("/api/v1/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.service").value("mavora"))
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void unknownApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/does-not-exist").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.stacktrace").doesNotExist());
    }

    @Test
    void incomingRequestIdIsPropagated() throws Exception {
        mockMvc.perform(get("/api/v1/health").header("X-Request-Id", "test-correlation"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Request-Id", "test-correlation"));
    }
}
