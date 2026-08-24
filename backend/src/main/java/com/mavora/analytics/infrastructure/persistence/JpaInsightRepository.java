package com.mavora.analytics.infrastructure.persistence;
import com.mavora.analytics.domain.Insight;
import com.mavora.analytics.domain.InsightRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaInsightRepository implements InsightRepository {
    private final InsightJpaRepository jpa;
    public JpaInsightRepository(InsightJpaRepository jpa) { this.jpa = jpa; }
    @Override public Insight save(Insight insight) {
        InsightEntity e = toEntity(insight); e.markNew(!jpa.existsById(insight.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<Insight> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private Insight toDomain(InsightEntity e) {
        return Insight.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getTitle(), e.getBody(), e.getCreatedAt(), e.getVersion());
    }
    private InsightEntity toEntity(Insight d) {
        InsightEntity e = new InsightEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setTitle(d.title()); e.setBody(d.body());
        e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
