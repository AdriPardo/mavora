package com.mavora.organization.domain;

import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository {

    OrganizationMember save(OrganizationMember member);

    Optional<OrganizationMember> findByOrganizationAndUser(OrganizationId organizationId, UserId userId);

    List<OrganizationMember> findByOrganization(OrganizationId organizationId);

    List<OrganizationMember> findByUser(UserId userId);
}
