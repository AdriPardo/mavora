package com.mavora.company.infrastructure.persistence;

import com.mavora.company.domain.Product;
import com.mavora.company.domain.ProductRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaProductRepository implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public JpaProductRepository(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = toEntity(product);
        entity.markNew(!jpaRepository.existsById(product.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<Product> findByCompany(UUID companyId) {
        return jpaRepository.findByCompanyIdOrderByCreatedAtAsc(companyId).stream()
                .map(JpaProductRepository::toDomain)
                .toList();
    }

    @Override
    public void deleteByCompany(UUID companyId, OrganizationId organizationId) {
        jpaRepository.deleteByCompanyIdAndOrganizationId(companyId, organizationId.value());
    }

    static Product toDomain(ProductEntity entity) {
        return Product.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getCompanyId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUrl(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    static ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.setId(product.id());
        entity.setOrganizationId(product.organizationId().value());
        entity.setCompanyId(product.companyId());
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setUrl(product.url());
        entity.setCreatedAt(product.createdAt());
        entity.setVersion(product.version());
        return entity;
    }
}
