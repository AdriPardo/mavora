package com.mavora.approvals.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApprovalRequestRepository {

    ApprovalRequest save(ApprovalRequest request);

    Optional<ApprovalRequest> findById(UUID id, OrganizationId organizationId);

    List<ApprovalRequest> findPending(OrganizationId organizationId);

    List<ApprovalRequest> findByOrganization(OrganizationId organizationId);

    long countPending(OrganizationId organizationId);
}
