package com.mavora.analytics.infrastructure.persistence;
import com.mavora.analytics.domain.MetricSnapshot;
import com.mavora.analytics.domain.MetricSnapshotRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaMetricSnapshotRepository implements MetricSnapshotRepository {
    private final MetricSnapshotJpaRepository jpa;
    public JpaMetricSnapshotRepository(MetricSnapshotJpaRepository jpa) { this.jpa = jpa; }
    @Override public MetricSnapshot save(MetricSnapshot snapshot) {
        MetricSnapshotEntity e = toEntity(snapshot); e.markNew(!jpa.existsById(snapshot.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<MetricSnapshot> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCapturedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private MetricSnapshot toDomain(MetricSnapshotEntity e) {
        return MetricSnapshot.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getMetric(), e.getValue(), e.getCapturedAt(), e.getSource(), e.getCreatedAt(), e.getVersion());
    }
    private MetricSnapshotEntity toEntity(MetricSnapshot d) {
        MetricSnapshotEntity e = new MetricSnapshotEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setMetric(d.metric()); e.setValue(d.value());
        e.setCapturedAt(d.capturedAt()); e.setSource(d.source()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
