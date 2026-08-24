package com.mavora.company.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;

public interface CompanyRepository {

    Company save(Company company);

    Optional<Company> findByOrganization(OrganizationId organizationId);
}
