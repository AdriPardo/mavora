package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;

public interface InstagramAccountRepository {

    InstagramAccount save(InstagramAccount account);

    Optional<InstagramAccount> findByOrganization(OrganizationId organizationId);
}
