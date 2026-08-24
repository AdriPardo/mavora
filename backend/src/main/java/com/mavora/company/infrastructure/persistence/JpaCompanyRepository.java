package com.mavora.company.infrastructure.persistence;

import com.mavora.company.domain.Company;
import com.mavora.company.domain.CompanyRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaCompanyRepository implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;

    public JpaCompanyRepository(CompanyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Company save(Company company) {
        CompanyEntity entity = toEntity(company);
        entity.markNew(!jpaRepository.existsById(company.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Company> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value()).map(JpaCompanyRepository::toDomain);
    }

    static Company toDomain(CompanyEntity entity) {
        return Company.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getName(),
                entity.getWebsiteUrl(),
                entity.getDescription(),
                entity.getMarket(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static CompanyEntity toEntity(Company company) {
        CompanyEntity entity = new CompanyEntity();
        entity.setId(company.id());
        entity.setOrganizationId(company.organizationId().value());
        entity.setName(company.name());
        entity.setWebsiteUrl(company.websiteUrl());
        entity.setDescription(company.description());
        entity.setMarket(company.market());
        entity.setCreatedAt(company.createdAt());
        entity.setUpdatedAt(company.updatedAt());
        entity.setVersion(company.version());
        return entity;
    }
}
