package com.mavora.identity.api;

import com.mavora.identity.application.CurrentAccount;
import java.util.List;
import java.util.UUID;

public record CurrentAccountResponse(UserResponse user, List<OrganizationMembershipResponse> organizations) {

    public static CurrentAccountResponse from(CurrentAccount account) {
        return new CurrentAccountResponse(
                new UserResponse(account.userId().value(), account.email()),
                account.organizations().stream()
                        .map(org -> new OrganizationMembershipResponse(
                                org.organizationId().value(),
                                org.name(),
                                org.slug(),
                                org.plan(),
                                org.status(),
                                org.role().name()
                        ))
                        .toList()
        );
    }

    public record UserResponse(UUID id, String email) {
    }

    public record OrganizationMembershipResponse(
            UUID id,
            String name,
            String slug,
            String plan,
            String status,
            String role
    ) {
    }
}
