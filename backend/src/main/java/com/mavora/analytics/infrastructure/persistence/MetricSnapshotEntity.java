package com.mavora.analytics.infrastructure.persistence;

import com.mavora.shared.infrastructure.persistence.OrgOwnedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "metric_snapshots")
public class MetricSnapshotEntity extends OrgOwnedEntity {
    @Column(nullable = false, length = 80) private String metric;
    @Column(nullable = false) private long value;
    @Column(name = "captured_at", nullable = false) private Instant capturedAt;
    @Column(nullable = false, length = 80) private String source;
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
