package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;

public interface MetaConnectionSettingsRepository {

    MetaConnectionSettings save(MetaConnectionSettings settings);

    Optional<MetaConnectionSettings> findByOrganization(OrganizationId organizationId);

    void delete(MetaConnectionSettings settings);
}
