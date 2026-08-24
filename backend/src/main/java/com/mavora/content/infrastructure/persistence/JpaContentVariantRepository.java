package com.mavora.content.infrastructure.persistence;
import com.mavora.content.domain.ContentVariant;
import com.mavora.content.domain.ContentVariantRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List; import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaContentVariantRepository implements ContentVariantRepository {
    private final ContentVariantJpaRepository jpa;
    public JpaContentVariantRepository(ContentVariantJpaRepository jpa) { this.jpa = jpa; }
    @Override public ContentVariant save(ContentVariant variant) {
        ContentVariantEntity e = toEntity(variant); e.markNew(!jpa.existsById(variant.id()));
        return toDomain(jpa.save(e));
    }
    @Override public List<ContentVariant> findByPiece(UUID pieceId) {
        return jpa.findByPieceIdOrderByCreatedAtAsc(pieceId).stream().map(this::toDomain).toList();
    }
    private ContentVariant toDomain(ContentVariantEntity e) {
        return ContentVariant.reconstitute(e.getId(), new OrganizationId(e.getOrganizationId()), e.getPieceId(), e.getChannel(), e.getBody(), e.getCreatedAt(), e.getVersion());
    }
    private ContentVariantEntity toEntity(ContentVariant d) {
        ContentVariantEntity e = new ContentVariantEntity();
        e.setId(d.id()); e.setOrganizationId(d.organizationId().value()); e.setPieceId(d.pieceId());
        e.setChannel(d.channel()); e.setBody(d.body()); e.setCreatedAt(d.createdAt()); e.setVersion(d.version());
        return e;
    }
}
