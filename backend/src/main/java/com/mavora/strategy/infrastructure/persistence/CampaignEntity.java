package com.mavora.strategy.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import com.mavora.strategy.domain.CampaignStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class CampaignEntity extends OrgOwnedEntity {

    @Column(name = "strategy_id", nullable = false)
    private UUID strategyId;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CampaignStatus status;

    @Column(name = "channels_json", nullable = false, columnDefinition = "TEXT")
    private String channelsJson;

    public UUID getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(UUID strategyId) {
        this.strategyId = strategyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public String getChannelsJson() {
        return channelsJson;
    }

    public void setChannelsJson(String channelsJson) {
        this.channelsJson = channelsJson;
    }
}
