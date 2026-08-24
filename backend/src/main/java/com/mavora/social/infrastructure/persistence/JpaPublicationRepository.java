package com.mavora.social.infrastructure.persistence;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.social.domain.Publication;
import com.mavora.social.domain.PublicationRepository;
import java.util.List; import java.util.Optional; import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaPublicationRepository implements PublicationRepository {
    private final PublicationJpaRepository jpa;
    public JpaPublicationRepository(PublicationJpaRepository jpa) { this.jpa = jpa; }
    @Override public Publication save(Publication publication) {
        PublicationEntity e = toEntity(publication); e.markNew(!jpa.existsById(publication.id()));
        return toDomain(jpa.save(e));
    }
    @Override public Optional<Publication> findById(UUID id, OrganizationId organizationId) {
        return jpa.findByIdAndOrganizationId(id, organizationId.value()).map(this::toDomain);
    }
    @Override public List<Publication> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    private Publication toDomain(PublicationEntity e) {
        return Publication.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getPieceId(), e.getChannel(), e.getCopy(), e.getStatus(), e.getPublishedAt(), e.getCreatedAt(), e.getVersion());
    }
    private PublicationEntity toEntity(Publication d) {
        PublicationEntity e = new PublicationEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setPieceId(d.pieceId());
        e.setChannel(d.channel()); e.setCopy(d.copy()); e.setStatus(d.status()); e.setPublishedAt(d.publishedAt());
        e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
