package com.mavora.analytics.infrastructure.persistence;
import com.mavora.analytics.domain.Learning;
import com.mavora.analytics.domain.LearningRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaLearningRepository implements LearningRepository {
    private final LearningJpaRepository jpa;
    public JpaLearningRepository(LearningJpaRepository jpa) { this.jpa = jpa; }
    @Override public Learning save(Learning learning) {
        LearningEntity e = toEntity(learning); e.markNew(!jpa.existsById(learning.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<Learning> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private Learning toDomain(LearningEntity e) {
        return Learning.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getInsightId(), e.getTitle(), e.getBody(), e.getCreatedAt(), e.getVersion());
    }
    private LearningEntity toEntity(Learning d) {
        LearningEntity e = new LearningEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setInsightId(d.insightId());
        e.setTitle(d.title()); e.setBody(d.body()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
