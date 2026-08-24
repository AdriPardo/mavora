package com.mavora.company.infrastructure.persistence;

import com.mavora.company.domain.GoalStatus;
import com.mavora.company.domain.MarketingGoal;
import com.mavora.company.domain.MarketingGoalRepository;
import com.mavora.shared.domain.Money;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaMarketingGoalRepository implements MarketingGoalRepository {

    private final MarketingGoalJpaRepository jpaRepository;

    public JpaMarketingGoalRepository(MarketingGoalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MarketingGoal save(MarketingGoal goal) {
        MarketingGoalEntity entity = toEntity(goal);
        entity.markNew(!jpaRepository.existsById(goal.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MarketingGoal> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value())
                .map(JpaMarketingGoalRepository::toDomain);
    }

    @Override
    public Optional<MarketingGoal> findActive(OrganizationId organizationId) {
        return jpaRepository.findFirstByOrganizationIdAndStatusOrderByCreatedAtDesc(
                        organizationId.value(), GoalStatus.ACTIVE
                )
                .map(JpaMarketingGoalRepository::toDomain);
    }

    @Override
    public List<MarketingGoal> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(JpaMarketingGoalRepository::toDomain)
                .toList();
    }

    static MarketingGoal toDomain(MarketingGoalEntity entity) {
        return MarketingGoal.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getMetric(),
                entity.getTargetValue(),
                entity.getDeadline(),
                Money.ofCents(entity.getBudgetCents(), entity.getBudgetCurrency()),
                entity.getMarket(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static MarketingGoalEntity toEntity(MarketingGoal goal) {
        MarketingGoalEntity entity = new MarketingGoalEntity();
        entity.setId(goal.id());
        entity.setOrganizationId(goal.organizationId().value());
        entity.setMetric(goal.metric());
        entity.setTargetValue(goal.targetValue());
        entity.setDeadline(goal.deadline());
        entity.setBudgetCents(goal.budget().amountCents());
        entity.setBudgetCurrency(goal.budget().currency().getCurrencyCode());
        entity.setMarket(goal.market());
        entity.setStatus(goal.status());
        entity.setCreatedAt(goal.createdAt());
        entity.setUpdatedAt(goal.updatedAt());
        entity.setVersion(goal.version());
        return entity;
    }
}
