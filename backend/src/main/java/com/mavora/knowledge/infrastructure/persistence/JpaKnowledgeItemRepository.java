package com.mavora.knowledge.infrastructure.persistence;

import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaKnowledgeItemRepository implements KnowledgeItemRepository {

    private final KnowledgeItemJpaRepository jpaRepository;

    public JpaKnowledgeItemRepository(KnowledgeItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public KnowledgeItem save(KnowledgeItem item) {
        KnowledgeItemEntity entity = toEntity(item);
        entity.markNew(!jpaRepository.existsById(item.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<KnowledgeItem> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(JpaKnowledgeItemRepository::toDomain)
                .toList();
    }

    @Override
    public List<KnowledgeItem> findRecent(OrganizationId organizationId, int limit) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(
                        organizationId.value(), PageRequest.of(0, limit)
                ).stream()
                .map(JpaKnowledgeItemRepository::toDomain)
                .toList();
    }

    static KnowledgeItem toDomain(KnowledgeItemEntity entity) {
        return KnowledgeItem.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getKind(),
                entity.getTitle(),
                entity.getBody(),
                entity.getSource(),
                entity.getConfidence(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    static KnowledgeItemEntity toEntity(KnowledgeItem item) {
        KnowledgeItemEntity entity = new KnowledgeItemEntity();
        entity.setId(item.id());
        entity.setOrganizationId(item.organizationId().value());
        entity.setKind(item.kind());
        entity.setTitle(item.title());
        entity.setBody(item.body());
        entity.setSource(item.source());
        entity.setConfidence(item.confidence());
        entity.setCreatedAt(item.createdAt());
        entity.setVersion(item.version());
        return entity;
    }
}
