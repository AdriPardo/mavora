package com.mavora.identity.application;

import com.mavora.organization.domain.Organization;
import com.mavora.organization.domain.OrganizationRole;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;

public record CurrentAccount(
        UserId userId,
        String email,
        List<Membership> organizations
) {

    public record Membership(
            OrganizationId organizationId,
            String name,
            String slug,
            String plan,
            String status,
            OrganizationRole role
    ) {
        public static Membership from(Organization organization, OrganizationRole role) {
            return new Membership(
                    organization.id(),
                    organization.name(),
                    organization.slug(),
                    organization.plan().name(),
                    organization.status().name(),
                    role
            );
        }
    }
}
