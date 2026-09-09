package com.mavora.organization.application;

import com.mavora.identity.domain.User;
import com.mavora.identity.domain.UserRepository;
import com.mavora.organization.domain.Organization;
import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.organization.domain.OrganizationRole;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public OrganizationQueryService(
            OrganizationAuthorizationService authorizationService,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.authorizationService = authorizationService;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public OrganizationView get(OrganizationId organizationId, UserId userId) {
        var access = authorizationService.requireMember(organizationId, userId);
        return OrganizationView.from(access.organization(), access.member().role());
    }

    @Transactional(readOnly = true)
    public List<MemberView> listMembers(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        List<MemberView> views = new ArrayList<>();
        for (OrganizationMember member : memberRepository.findByOrganization(organizationId)) {
            String email = userRepository.findById(member.userId())
                    .map(User::email)
                    .map(value -> value.normalized())
                    .orElse("unknown");
            views.add(new MemberView(member.userId(), email, member.role().name(), member.createdAt()));
        }
        return List.copyOf(views);
    }

    public record OrganizationView(
            OrganizationId id,
            String name,
            String slug,
            String plan,
            String status,
            String role
    ) {
        static OrganizationView from(Organization organization, OrganizationRole role) {
            return new OrganizationView(
                    organization.id(),
                    organization.name(),
                    organization.slug(),
                    organization.plan().name(),
                    organization.status().name(),
                    role.name()
            );
        }
    }

    public record MemberView(UserId userId, String email, String role, Instant createdAt) {
    }
}
