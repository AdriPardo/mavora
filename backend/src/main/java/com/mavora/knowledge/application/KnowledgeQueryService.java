package com.mavora.knowledge.application;

import com.mavora.knowledge.domain.KnowledgeItem;
import com.mavora.knowledge.domain.KnowledgeItemRepository;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.UserId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeQueryService {

    private final OrganizationAuthorizationService authorizationService;
    private final KnowledgeItemRepository knowledgeItemRepository;

    public KnowledgeQueryService(
            OrganizationAuthorizationService authorizationService,
            KnowledgeItemRepository knowledgeItemRepository
    ) {
        this.authorizationService = authorizationService;
        this.knowledgeItemRepository = knowledgeItemRepository;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeItem> list(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return knowledgeItemRepository.findByOrganization(organizationId);
    }
}
