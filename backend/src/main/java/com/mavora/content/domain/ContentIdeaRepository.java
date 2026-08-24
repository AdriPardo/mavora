package com.mavora.content.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;

public interface ContentIdeaRepository {

    ContentIdea save(ContentIdea idea);

    List<ContentIdea> findByOrganization(OrganizationId organizationId);
}
