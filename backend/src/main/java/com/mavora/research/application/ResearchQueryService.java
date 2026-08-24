package com.mavora.research.application;

import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.research.domain.Competitor;
import com.mavora.research.domain.CompetitorRepository;
import com.mavora.research.domain.IcpProfile;
import com.mavora.research.domain.IcpProfileRepository;
import com.mavora.research.domain.Persona;
import com.mavora.research.domain.PersonaRepository;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearchQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final PersonaRepository personaRepository;
    private final CompetitorRepository competitorRepository;
    private final IcpProfileRepository icpProfileRepository;

    public ResearchQueryService(
            OrganizationAuthorizationService authorizationService,
            PersonaRepository personaRepository,
            CompetitorRepository competitorRepository,
            IcpProfileRepository icpProfileRepository
    ) {
        this.authorizationService = authorizationService;
        this.personaRepository = personaRepository;
        this.competitorRepository = competitorRepository;
        this.icpProfileRepository = icpProfileRepository;
    }

    @Transactional(readOnly = true)
    public ResearchView get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return new ResearchView(
                personaRepository.findByOrganization(organizationId),
                competitorRepository.findByOrganization(organizationId),
                icpProfileRepository.findLatest(organizationId)
        );
    }

    public record ResearchView(List<Persona> personas, List<Competitor> competitors, Optional<IcpProfile> icp) {
    }
}
