package com.mavora.content.infrastructure.persistence;
import com.mavora.content.domain.ContentPiece;
import com.mavora.content.domain.ContentPieceRepository;
import com.mavora.content.domain.PieceStatus;
import com.mavora.shared.domain.OrganizationId;
import java.util.List; import java.util.Optional; import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaContentPieceRepository implements ContentPieceRepository {
    private final ContentPieceJpaRepository jpa;
    public JpaContentPieceRepository(ContentPieceJpaRepository jpa) { this.jpa = jpa; }
    @Override public ContentPiece save(ContentPiece piece) {
        ContentPieceEntity e = toEntity(piece); e.markNew(!jpa.existsById(piece.id()));
        return toDomain(jpa.save(e));
    }
    @Override public Optional<ContentPiece> findById(UUID id, OrganizationId organizationId) {
        return jpa.findByIdAndOrganizationId(id, organizationId.value()).map(this::toDomain);
    }
    @Override public List<ContentPiece> findByOrganization(OrganizationId organizationId) {
        return jpa.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream().map(this::toDomain).toList();
    }
    @Override public Optional<ContentPiece> findLatestApproved(OrganizationId organizationId) {
        return jpa.findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(organizationId.value(), PieceStatus.APPROVED).map(this::toDomain);
    }
    private ContentPiece toDomain(ContentPieceEntity e) {
        return ContentPiece.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getIdeaId(), e.getTitle(), e.getBody(), e.getStatus(), e.getCreatedAt(), e.getUpdatedAt(), e.getVersion());
    }
    private ContentPieceEntity toEntity(ContentPiece d) {
        ContentPieceEntity e = new ContentPieceEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setIdeaId(d.ideaId()); e.setTitle(d.title());
        e.setBody(d.body()); e.setStatus(d.status()); e.setCreatedAt(d.createdAt()); e.setUpdatedAt(d.updatedAt()); e.setVersion(d.version());
        return e;
    }
}
