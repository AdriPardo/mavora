package com.mavora.company.infrastructure.persistence;

import com.mavora.company.domain.GoalStatus;
import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "marketing_goals")
public class MarketingGoalEntity extends OrgOwnedEntity {

    @Column(nullable = false, length = 80)
    private String metric;

    @Column(name = "target_value", nullable = false)
    private long targetValue;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(name = "budget_cents", nullable = false)
    private long budgetCents;

    @Column(name = "budget_currency", nullable = false, length = 3)
    private String budgetCurrency;

    @Column(nullable = false, length = 200)
    private String market;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private GoalStatus status;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public long getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(long targetValue) {
        this.targetValue = targetValue;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public long getBudgetCents() {
        return budgetCents;
    }

    public void setBudgetCents(long budgetCents) {
        this.budgetCents = budgetCents;
    }

    public String getBudgetCurrency() {
        return budgetCurrency;
    }

    public void setBudgetCurrency(String budgetCurrency) {
        this.budgetCurrency = budgetCurrency;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
