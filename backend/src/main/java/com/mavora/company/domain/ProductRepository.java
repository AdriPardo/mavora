package com.mavora.company.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    List<Product> findByCompany(UUID companyId);

    void deleteByCompany(UUID companyId, OrganizationId organizationId);
}
