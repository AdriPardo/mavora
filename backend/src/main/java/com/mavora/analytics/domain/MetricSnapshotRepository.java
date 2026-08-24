package com.mavora.analytics.domain;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
public interface MetricSnapshotRepository {
    MetricSnapshot save(MetricSnapshot snapshot);
    List<MetricSnapshot> findByOrganization(OrganizationId organizationId);
}
