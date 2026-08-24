package com.mavora.social.application;

import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import com.mavora.shared.domain.UserId;
import com.mavora.social.domain.Publication;
import com.mavora.social.domain.PublicationRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationService {

    private final OrganizationAuthorizationService authorizationService;
    private final PublicationRepository publicationRepository;
    private final Clock clock;

    public PublicationService(
            OrganizationAuthorizationService authorizationService,
            PublicationRepository publicationRepository,
            Clock clock
    ) {
        this.authorizationService = authorizationService;
        this.publicationRepository = publicationRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<Publication> list(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return publicationRepository.findByOrganization(organizationId);
    }

    @Transactional
    public Publication publish(OrganizationId organizationId, UserId userId, UUID publicationId) {
        authorizationService.requireWriter(organizationId, userId);
        Publication publication = publicationRepository.findById(publicationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Publication not found"));
        publication.publish(clock.instant());
        return publicationRepository.save(publication);
    }
}
