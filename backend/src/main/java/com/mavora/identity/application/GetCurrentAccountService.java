package com.mavora.identity.application;

import com.mavora.identity.domain.User;
import com.mavora.identity.domain.UserRepository;
import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.organization.domain.OrganizationRepository;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.UserId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCurrentAccountService {

    private final UserRepository userRepository;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;

    public GetCurrentAccountService(
            UserRepository userRepository,
            OrganizationMemberRepository memberRepository,
            OrganizationRepository organizationRepository
    ) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public CurrentAccount get(UserId userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DomainException(
                        DomainException.ErrorType.UNAUTHENTICATED,
                        "Unauthorized",
                        "Authentication is required"
                ));
        List<OrganizationMember> memberships = memberRepository.findByUser(userId);
        List<CurrentAccount.Membership> organizations = new ArrayList<>();
        for (OrganizationMember membership : memberships) {
            organizationRepository.findById(membership.organizationId()).ifPresent(organization ->
                    organizations.add(CurrentAccount.Membership.from(organization, membership.role()))
            );
        }
        return new CurrentAccount(user.id(), user.email().normalized(), List.copyOf(organizations));
    }
}
