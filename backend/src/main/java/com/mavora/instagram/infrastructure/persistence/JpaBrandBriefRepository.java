package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.BrandBrief;
import com.mavora.instagram.domain.BrandBriefRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaBrandBriefRepository implements BrandBriefRepository {

    private final BrandBriefJpaRepository jpaRepository;

    public JpaBrandBriefRepository(BrandBriefJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BrandBrief save(BrandBrief brief) {
        BrandBriefEntity entity = toEntity(brief);
        entity.markNew(!jpaRepository.existsById(brief.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<BrandBrief> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).map(JpaBrandBriefRepository::toDomain);
    }

    static BrandBrief toDomain(BrandBriefEntity entity) {
        return BrandBrief.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getVoice(),
                entity.getOffer(),
                entity.getCta(),
                entity.getAudience(),
                entity.getExtraNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static BrandBriefEntity toEntity(BrandBrief brief) {
        BrandBriefEntity entity = new BrandBriefEntity();
        entity.setId(brief.id());
        entity.setOrganizationId(brief.organizationId().value());
        entity.setVoice(brief.voice());
        entity.setOffer(brief.offer());
        entity.setCta(brief.cta());
        entity.setAudience(brief.audience());
        entity.setExtraNotes(brief.extraNotes());
        entity.setCreatedAt(brief.createdAt());
        entity.setUpdatedAt(brief.updatedAt());
        entity.setVersion(brief.version());
        return entity;
    }
}
