package com.mavora.strategy.infrastructure.persistence;

import com.mavora.shared.domain.OrganizationId;
import com.mavora.strategy.domain.MarketingStrategy;
import com.mavora.strategy.domain.MarketingStrategyRepository;
import com.mavora.strategy.domain.StrategyStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaMarketingStrategyRepository implements MarketingStrategyRepository {

    private final MarketingStrategyJpaRepository jpaRepository;

    public JpaMarketingStrategyRepository(MarketingStrategyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MarketingStrategy save(MarketingStrategy strategy) {
        MarketingStrategyEntity entity = toEntity(strategy);
        entity.markNew(!jpaRepository.existsById(strategy.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MarketingStrategy> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value()).map(this::toDomain);
    }

    @Override
    public Optional<MarketingStrategy> findLatest(OrganizationId organizationId) {
        return jpaRepository.findFirstByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).map(this::toDomain);
    }

    @Override
    public Optional<MarketingStrategy> findApproved(OrganizationId organizationId) {
        return jpaRepository.findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
                organizationId.value(), StrategyStatus.APPROVED
        ).map(this::toDomain);
    }

    @Override
    public List<MarketingStrategy> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    private MarketingStrategy toDomain(MarketingStrategyEntity entity) {
        return MarketingStrategy.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getCompanyId(),
                entity.getGoalId(),
                entity.getWorkflowId(),
                entity.getStatus(),
                entity.getPositioning(),
                entity.getIcpSummary(),
                entity.getChannelsJson(),
                entity.getPillarsJson(),
                entity.getKpisJson(),
                entity.getNarrative(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    private MarketingStrategyEntity toEntity(MarketingStrategy strategy) {
        MarketingStrategyEntity entity = new MarketingStrategyEntity();
        entity.setId(strategy.id());
        entity.setOrganizationId(strategy.organizationId().value());
        entity.setCompanyId(strategy.companyId());
        entity.setGoalId(strategy.goalId());
        entity.setWorkflowId(strategy.workflowId());
        entity.setStatus(strategy.status());
        entity.setPositioning(strategy.positioning());
        entity.setIcpSummary(strategy.icpSummary());
        entity.setChannelsJson(strategy.channelsJson());
        entity.setPillarsJson(strategy.pillarsJson());
        entity.setKpisJson(strategy.kpisJson());
        entity.setNarrative(strategy.narrative());
        entity.setCreatedAt(strategy.createdAt());
        entity.setUpdatedAt(strategy.updatedAt());
        entity.setVersion(strategy.version());
        return entity;
    }
}
