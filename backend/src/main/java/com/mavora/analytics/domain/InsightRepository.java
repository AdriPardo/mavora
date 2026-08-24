package com.mavora.analytics.domain;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
public interface InsightRepository {
    Insight save(Insight insight);
    List<Insight> findByOrganization(OrganizationId organizationId);
}
