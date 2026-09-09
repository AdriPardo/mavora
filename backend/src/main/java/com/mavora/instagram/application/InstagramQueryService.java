package com.mavora.instagram.application;

import com.mavora.instagram.domain.InstagramPlaybook;
import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.instagram.domain.InstagramSlotRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstagramQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final InstagramSlotRepository slotRepository;
    private final String llmProvider;
    private final String mediaProvider;

    public InstagramQueryService(
            OrganizationAuthorizationService authorizationService,
            InstagramSlotRepository slotRepository,
            @Value("${mavora.llm.provider:deepseek}") String llmProvider,
            @Value("${mavora.fal.provider:fal}") String mediaProvider
    ) {
        this.authorizationService = authorizationService;
        this.slotRepository = slotRepository;
        this.llmProvider = llmProvider;
        this.mediaProvider = mediaProvider;
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
                        .toList(),
                llmProvider,
                mediaProvider
        );
    }

    public record Playbook(
            String timezone,
            String principles,
            List<PlaybookSlot> mix,
            String llmProvider,
            String mediaProvider
    ) {
    }

    public record PlaybookSlot(String day, String time, String format) {
    }
}
