package com.mavora.organization.application;

import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.organization.domain.OrganizationNotFoundException;
import com.mavora.organization.domain.OrganizationRepository;
import com.mavora.organization.domain.OrganizationRole;
import com.mavora.shared.domain.ForbiddenActionException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationAuthorizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;

    public OrganizationAuthorizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public MembershipAccess requireMember(OrganizationId organizationId, UserId userId) {
        var organization = organizationRepository.findById(organizationId)
                .orElseThrow(OrganizationNotFoundException::new);
        OrganizationMember member = memberRepository.findByOrganizationAndUser(organizationId, userId)
                .orElseThrow(OrganizationNotFoundException::new);
        return new MembershipAccess(organization, member);
    }

    @Transactional(readOnly = true)
    public MembershipAccess requireWriter(OrganizationId organizationId, UserId userId) {
        MembershipAccess access = requireMember(organizationId, userId);
        if (access.member().role() == OrganizationRole.VIEWER) {
            throw new ForbiddenActionException();
        }
        return access;
    }

    public record MembershipAccess(
            com.mavora.organization.domain.Organization organization,
            OrganizationMember member
    ) {
    }
}
