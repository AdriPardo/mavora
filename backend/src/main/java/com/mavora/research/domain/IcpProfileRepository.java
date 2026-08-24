package com.mavora.research.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;

public interface IcpProfileRepository {

    IcpProfile save(IcpProfile profile);
    Optional<IcpProfile> findLatest(OrganizationId organizationId);

}
