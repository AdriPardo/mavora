package com.mavora.company.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    List<ProductEntity> findByCompanyIdOrderByCreatedAtAsc(UUID companyId);

    void deleteByCompanyIdAndOrganizationId(UUID companyId, UUID organizationId);
}
