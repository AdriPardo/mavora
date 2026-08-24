package com.mavora.research.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "personas")
public class PersonaEntity extends OrgOwnedEntity {
    @Column(name = "strategy_id", nullable = false) private UUID strategyId;
    @Column(nullable = false, length = 120) private String name;
    @Column(nullable = false, columnDefinition = "TEXT") private String summary;
    @Column(nullable = false, columnDefinition = "TEXT") private String pains;
    @Column(nullable = false, columnDefinition = "TEXT") private String jobs;
    public UUID getStrategyId() { return strategyId; }
    public void setStrategyId(UUID strategyId) { this.strategyId = strategyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getPains() { return pains; }
    public void setPains(String pains) { this.pains = pains; }
    public String getJobs() { return jobs; }
    public void setJobs(String jobs) { this.jobs = jobs; }
}
