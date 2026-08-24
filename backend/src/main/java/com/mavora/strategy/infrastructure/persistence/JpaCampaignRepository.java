package com.mavora.strategy.infrastructure.persistence;

import com.mavora.shared.domain.OrganizationId;
import com.mavora.strategy.domain.Campaign;
import com.mavora.strategy.domain.CampaignRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaCampaignRepository implements CampaignRepository {

    private final CampaignJpaRepository jpaRepository;

    public JpaCampaignRepository(CampaignJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Campaign save(Campaign campaign) {
        CampaignEntity entity = toEntity(campaign);
        entity.markNew(!jpaRepository.existsById(campaign.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Campaign> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private Campaign toDomain(CampaignEntity entity) {
        return Campaign.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getStrategyId(),
                entity.getName(),
                entity.getStatus(),
                entity.getChannelsJson(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    private CampaignEntity toEntity(Campaign campaign) {
        CampaignEntity entity = new CampaignEntity();
        entity.setId(campaign.id());
        entity.setOrganizationId(campaign.organizationId().value());
        entity.setStrategyId(campaign.strategyId());
        entity.setName(campaign.name());
        entity.setStatus(campaign.status());
        entity.setChannelsJson(campaign.channelsJson());
        entity.setCreatedAt(campaign.createdAt());
        entity.setVersion(campaign.version());
        return entity;
    }
}
