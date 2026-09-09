package com.mavora.organization.infrastructure.persistence;

import com.mavora.organization.domain.Organization;
import com.mavora.organization.domain.OrganizationRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaOrganizationRepository implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    public JpaOrganizationRepository(OrganizationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Organization save(Organization organization) {
        OrganizationEntity entity = toEntity(organization);
        entity.markNew(!jpaRepository.existsById(organization.id().value()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Organization> findById(OrganizationId id) {
        return jpaRepository.findById(id.value()).map(JpaOrganizationRepository::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpaRepository.existsBySlug(slug);
    }

    static Organization toDomain(OrganizationEntity entity) {
        return Organization.reconstitute(
                new OrganizationId(entity.getId()),
                entity.getName(),
                entity.getSlug(),
                entity.getPlan(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static OrganizationEntity toEntity(Organization organization) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(organization.id().value());
        entity.setName(organization.name());
        entity.setSlug(organization.slug());
        entity.setPlan(organization.plan());
        entity.setStatus(organization.status());
        entity.setCreatedAt(organization.createdAt());
        entity.setUpdatedAt(organization.updatedAt());
        entity.setVersion(organization.version());
        return entity;
    }
}
