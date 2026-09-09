package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.MediaAsset;
import com.mavora.instagram.domain.MediaAssetRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaMediaAssetRepository implements MediaAssetRepository {

    private final MediaAssetJpaRepository jpaRepository;

    public JpaMediaAssetRepository(MediaAssetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MediaAsset save(MediaAsset asset) {
        MediaAssetEntity entity = toEntity(asset);
        entity.markNew(!jpaRepository.existsById(asset.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MediaAsset> findById(UUID id) {
        return jpaRepository.findById(id).map(JpaMediaAssetRepository::toDomain);
    }

    @Override
    public Optional<MediaAsset> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value())
                .map(JpaMediaAssetRepository::toDomain);
    }

    @Override
    public List<MediaAsset> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId.value()).stream()
                .map(JpaMediaAssetRepository::toDomain)
                .toList();
    }

    @Override
    public void delete(UUID id, OrganizationId organizationId) {
        jpaRepository.findByIdAndOrganizationId(id, organizationId.value()).ifPresent(jpaRepository::delete);
    }

    static MediaAsset toDomain(MediaAssetEntity entity) {
        return MediaAsset.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getProductId(),
                entity.getKind(),
                entity.getFilename(),
                entity.getContentType(),
                entity.getStoragePath(),
                entity.getByteSize(),
                entity.getCaptionHint(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    static MediaAssetEntity toEntity(MediaAsset asset) {
        MediaAssetEntity entity = new MediaAssetEntity();
        entity.setId(asset.id());
        entity.setOrganizationId(asset.organizationId().value());
        entity.setProductId(asset.productId());
        entity.setKind(asset.kind());
        entity.setFilename(asset.filename());
        entity.setContentType(asset.contentType());
        entity.setStoragePath(asset.storagePath());
        entity.setByteSize(asset.byteSize());
        entity.setCaptionHint(asset.captionHint());
        entity.setCreatedAt(asset.createdAt());
        entity.setVersion(asset.version());
        return entity;
    }
}
