package com.mavora.analytics.application;

import com.mavora.analytics.domain.Insight;
import com.mavora.analytics.domain.InsightRepository;
import com.mavora.analytics.domain.Learning;
import com.mavora.analytics.domain.LearningRepository;
import com.mavora.analytics.domain.MetricSnapshot;
import com.mavora.analytics.domain.MetricSnapshotRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private final OrganizationAuthorizationService authorizationService;
    private final MetricSnapshotRepository snapshotRepository;
    private final InsightRepository insightRepository;
    private final LearningRepository learningRepository;
    private final Clock clock;

    public AnalyticsService(
            OrganizationAuthorizationService authorizationService,
            MetricSnapshotRepository snapshotRepository,
            InsightRepository insightRepository,
            LearningRepository learningRepository,
            Clock clock
    ) {
        this.authorizationService = authorizationService;
        this.snapshotRepository = snapshotRepository;
        this.insightRepository = insightRepository;
        this.learningRepository = learningRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AnalyticsView get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return new AnalyticsView(
                snapshotRepository.findByOrganization(organizationId),
                insightRepository.findByOrganization(organizationId),
                learningRepository.findByOrganization(organizationId)
        );
    }

    @Transactional
    public MetricSnapshot record(
            OrganizationId organizationId,
            UserId userId,
            String metric,
            long value,
            Instant capturedAt,
            String source
    ) {
        authorizationService.requireWriter(organizationId, userId);
        Instant now = clock.instant();
        return snapshotRepository.save(MetricSnapshot.create(
                organizationId,
                metric,
                value,
                capturedAt == null ? now : capturedAt,
                source == null || source.isBlank() ? "manual" : source,
                now
        ));
    }

    public record AnalyticsView(
            List<MetricSnapshot> snapshots,
            List<Insight> insights,
            List<Learning> learnings
    ) {
    }
}
