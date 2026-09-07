package com.mavora.instagram.application;

import com.mavora.instagram.domain.BrandBrief;
import com.mavora.instagram.domain.BrandBriefRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandBriefService {

    private final OrganizationAuthorizationService authorizationService;
    private final BrandBriefRepository briefRepository;
    private final Clock clock;

    public BrandBriefService(
            OrganizationAuthorizationService authorizationService,
            BrandBriefRepository briefRepository,
            Clock clock
    ) {
        this.authorizationService = authorizationService;
        this.briefRepository = briefRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Optional<BrandBrief> get(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return briefRepository.findByOrganization(organizationId);
    }

    @Transactional
    public BrandBrief upsert(
            OrganizationId organizationId,
            UserId userId,
            String voice,
            String offer,
            String cta,
            String audience,
            String extraNotes
    ) {
        authorizationService.requireWriter(organizationId, userId);
        Instant now = clock.instant();
        return briefRepository.findByOrganization(organizationId)
                .map(existing -> {
                    existing.update(voice, offer, cta, audience, extraNotes, now);
                    return briefRepository.save(existing);
                })
                .orElseGet(() -> briefRepository.save(BrandBrief.create(
                        organizationId, voice, offer, cta, audience, extraNotes, now
                )));
    }
}
