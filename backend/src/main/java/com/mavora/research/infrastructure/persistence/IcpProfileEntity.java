package com.mavora.research.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "icp_profiles")
public class IcpProfileEntity extends OrgOwnedEntity {
    @Column(name = "strategy_id", nullable = false) private UUID strategyId;
    @Column(nullable = false, columnDefinition = "TEXT") private String summary;
    @Column(name = "segments_json", nullable = false, columnDefinition = "TEXT") private String segmentsJson;
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSegmentsJson() { return segmentsJson; }
    public void setSegmentsJson(String segmentsJson) { this.segmentsJson = segmentsJson; }
}
