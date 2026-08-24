package com.mavora.analytics.domain;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
public interface LearningRepository {
    Learning save(Learning learning);
    List<Learning> findByOrganization(OrganizationId organizationId);
}
