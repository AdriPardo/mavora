package com.mavora.shared.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Health")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Liveness for the SPA and local tooling")
    public HealthResponse health() {
        return new HealthResponse("ok", "mavora");
    }

    public record HealthResponse(String status, String service) {
    }
}
