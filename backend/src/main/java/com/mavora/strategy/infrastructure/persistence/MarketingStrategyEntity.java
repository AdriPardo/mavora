package com.mavora.strategy.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import com.mavora.strategy.domain.StrategyStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketing_strategies")
public class MarketingStrategyEntity extends OrgOwnedEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "goal_id", nullable = false)
    private UUID goalId;

    @Column(name = "workflow_id")
    private UUID workflowId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private StrategyStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String positioning;

    @Column(name = "icp_summary", nullable = false, columnDefinition = "TEXT")
    private String icpSummary;

    @Column(name = "channels_json", nullable = false, columnDefinition = "TEXT")
    private String channelsJson;

    @Column(name = "pillars_json", nullable = false, columnDefinition = "TEXT")
    private String pillarsJson;

    @Column(name = "kpis_json", nullable = false, columnDefinition = "TEXT")
    private String kpisJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getGoalId() {
        return goalId;
    }

    public void setGoalId(UUID goalId) {
        this.goalId = goalId;
    }

    public UUID getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(UUID workflowId) {
        this.workflowId = workflowId;
    }

    public StrategyStatus getStatus() {
        return status;
    }

    public void setStatus(StrategyStatus status) {
        this.status = status;
    }

    public String getPositioning() {
        return positioning;
    }

    public void setPositioning(String positioning) {
        this.positioning = positioning;
    }

    public String getIcpSummary() {
        return icpSummary;
    }

    public void setIcpSummary(String icpSummary) {
        this.icpSummary = icpSummary;
    }

    public String getChannelsJson() {
        return channelsJson;
    }

    public void setChannelsJson(String channelsJson) {
        this.channelsJson = channelsJson;
    }

    public String getPillarsJson() {
        return pillarsJson;
    }

    public void setPillarsJson(String pillarsJson) {
        this.pillarsJson = pillarsJson;
    }

    public String getKpisJson() {
        return kpisJson;
    }

    public void setKpisJson(String kpisJson) {
        this.kpisJson = kpisJson;
    }

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
