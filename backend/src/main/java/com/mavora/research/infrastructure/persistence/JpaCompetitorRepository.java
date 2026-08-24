package com.mavora.research.infrastructure.persistence;
import com.mavora.research.domain.Competitor;
import com.mavora.research.domain.CompetitorRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaCompetitorRepository implements CompetitorRepository {
    private final CompetitorJpaRepository jpa;
    public JpaCompetitorRepository(CompetitorJpaRepository jpa) { this.jpa = jpa; }
    @Override public Competitor save(Competitor competitor) {
        CompetitorEntity e = toEntity(competitor); e.markNew(!jpa.existsById(competitor.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<Competitor> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private Competitor toDomain(CompetitorEntity e) {
        return Competitor.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getName(), e.getUrl(), e.getNotes(), e.getCreatedAt(), e.getVersion());
    }
    private CompetitorEntity toEntity(Competitor d) {
        CompetitorEntity e = new CompetitorEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setName(d.name());
        e.setUrl(d.url()); e.setNotes(d.notes()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
