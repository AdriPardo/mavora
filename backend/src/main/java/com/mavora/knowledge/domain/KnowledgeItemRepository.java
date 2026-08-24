package com.mavora.knowledge.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;

public interface KnowledgeItemRepository {

    KnowledgeItem save(KnowledgeItem item);

    List<KnowledgeItem> findByOrganization(OrganizationId organizationId);

    List<KnowledgeItem> findRecent(OrganizationId organizationId, int limit);
}
