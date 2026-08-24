package com.mavora.analytics.api;

import com.mavora.analytics.application.AnalyticsService;
import com.mavora.analytics.domain.Insight;
import com.mavora.analytics.domain.Learning;
import com.mavora.analytics.domain.MetricSnapshot;
import com.mavora.identity.application.MavoraPrincipal;
import com.mavora.shared.domain.OrganizationId;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/organizations/{organizationId}/analytics", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public AnalyticsResponse get(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal
    ) {
        var view = analyticsService.get(new OrganizationId(organizationId), principal.userId());
        return new AnalyticsResponse(
                view.snapshots().stream().map(SnapshotResponse::from).toList(),
                view.insights().stream().map(InsightResponse::from).toList(),
                view.learnings().stream().map(LearningResponse::from).toList()
        );
    }

    @PostMapping(value = "/snapshots", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SnapshotResponse record(
            @PathVariable UUID organizationId,
            @AuthenticationPrincipal MavoraPrincipal principal,
            @Valid @RequestBody RecordSnapshotRequest request
    ) {
        return SnapshotResponse.from(analyticsService.record(
                new OrganizationId(organizationId),
                principal.userId(),
                request.metric(),
                request.value(),
                request.capturedAt(),
                request.source()
        ));
    }

    public record RecordSnapshotRequest(
            @NotBlank @Size(max = 80) String metric,
            long value,
            Instant capturedAt,
            @Size(max = 80) String source
    ) {
    }

    public record AnalyticsResponse(
            List<SnapshotResponse> snapshots,
            List<InsightResponse> insights,
            List<LearningResponse> learnings
    ) {
    }

    public record SnapshotResponse(UUID id, String metric, long value, Instant capturedAt, String source) {
        static SnapshotResponse from(MetricSnapshot snapshot) {
            return new SnapshotResponse(
                    snapshot.id(), snapshot.metric(), snapshot.value(), snapshot.capturedAt(), snapshot.source()
            );
        }
    }

    public record InsightResponse(UUID id, String title, String body, Instant createdAt) {
        static InsightResponse from(Insight insight) {
            return new InsightResponse(insight.id(), insight.title(), insight.body(), insight.createdAt());
        }
    }

    public record LearningResponse(UUID id, UUID insightId, String title, String body, Instant createdAt) {
        static LearningResponse from(Learning learning) {
            return new LearningResponse(
                    learning.id(), learning.insightId(), learning.title(), learning.body(), learning.createdAt()
            );
        }
    }
}
