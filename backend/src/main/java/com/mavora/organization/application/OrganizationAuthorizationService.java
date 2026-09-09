package com.mavora.organization.application;

import com.mavora.organization.domain.Organization;
import com.mavora.organization.domain.OrganizationMember;
import com.mavora.organization.domain.OrganizationMemberRepository;
import com.mavora.organization.domain.OrganizationNotFoundException;
import com.mavora.organization.domain.OrganizationRepository;
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
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(OrganizationNotFoundException::new);
        OrganizationMember member = memberRepository.findByOrganizationAndUser(organizationId, userId)
                .orElseThrow(OrganizationNotFoundException::new);
        return new MembershipAccess(organization, member);
    }

    public record MembershipAccess(Organization organization, OrganizationMember member) {
    }
}
