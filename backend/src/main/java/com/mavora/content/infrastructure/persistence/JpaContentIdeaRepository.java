package com.mavora.content.infrastructure.persistence;
import com.mavora.content.domain.ContentIdea;
import com.mavora.content.domain.ContentIdeaRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaContentIdeaRepository implements ContentIdeaRepository {
    private final ContentIdeaJpaRepository jpa;
    public JpaContentIdeaRepository(ContentIdeaJpaRepository jpa) { this.jpa = jpa; }
    @Override public ContentIdea save(ContentIdea idea) {
        ContentIdeaEntity e = toEntity(idea); e.markNew(!jpa.existsById(idea.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<ContentIdea> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private ContentIdea toDomain(ContentIdeaEntity e) {
        return ContentIdea.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getTitle(), e.getAngle(), e.getPillar(), e.getStatus(), e.getCreatedAt(), e.getVersion());
    }
    private ContentIdeaEntity toEntity(ContentIdea d) {
        ContentIdeaEntity e = new ContentIdeaEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setTitle(d.title()); e.setAngle(d.angle());
        e.setPillar(d.pillar()); e.setStatus(d.status()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
