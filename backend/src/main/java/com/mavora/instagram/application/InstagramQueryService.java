package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramPlaybook;
import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.instagram.domain.InstagramSlotRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final InstagramSlotRepository slotRepository;

    public InstagramQueryService(
            OrganizationAuthorizationService authorizationService,
            InstagramSlotRepository slotRepository
    ) {
        this.authorizationService = authorizationService;
        this.slotRepository = slotRepository;
    }

    @Transactional(readOnly = true)
    public List<InstagramSlot> slots(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return slotRepository.findByOrganization(organizationId);
    }

    @Transactional(readOnly = true)
    public Playbook playbook(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return new Playbook(
                InstagramPlaybook.ZONE.getId(),
                InstagramPlaybook.PRINCIPLES,
                InstagramPlaybook.weekMix().stream()
                        .map(item -> new PlaybookSlot(item.day().name(), item.time().toString(), item.format().name()))
                        .toList()
        );
    }

    public record Playbook(String timezone, String principles, List<PlaybookSlot> mix) {
    }

    public record PlaybookSlot(String day, String time, String format) {
    }
}
