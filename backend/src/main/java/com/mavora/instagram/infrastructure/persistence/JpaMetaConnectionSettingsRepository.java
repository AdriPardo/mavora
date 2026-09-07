package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.MetaConnectionSettings;
import com.mavora.instagram.domain.MetaConnectionSettingsRepository;
import com.mavora.shared.domain.OrganizationId;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class JpaMetaConnectionSettingsRepository implements MetaConnectionSettingsRepository {

    private final MetaConnectionSettingsJpaRepository jpaRepository;

    public JpaMetaConnectionSettingsRepository(MetaConnectionSettingsJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MetaConnectionSettings save(MetaConnectionSettings settings) {
        MetaConnectionSettingsEntity entity = toEntity(settings);
        entity.markNew(!jpaRepository.existsById(settings.id()));
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MetaConnectionSettings> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationId(organizationId.value())
                .map(JpaMetaConnectionSettingsRepository::toDomain);
    }

    @Override
    public void delete(MetaConnectionSettings settings) {
        jpaRepository.deleteById(settings.id());
    }

    static MetaConnectionSettings toDomain(MetaConnectionSettingsEntity entity) {
        return MetaConnectionSettings.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getAppId(),
                entity.getAppSecretCiphertext(),
                entity.getRedirectUri(),
                entity.getGraphVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion()
        );
    }

    static MetaConnectionSettingsEntity toEntity(MetaConnectionSettings settings) {
        MetaConnectionSettingsEntity entity = new MetaConnectionSettingsEntity();
        entity.setId(settings.id());
        entity.setOrganizationId(settings.organizationId().value());
        entity.setAppId(settings.appId());
        entity.setAppSecretCiphertext(settings.appSecretCiphertext());
        entity.setRedirectUri(settings.redirectUri());
        entity.setGraphVersion(settings.graphVersion());
        entity.setCreatedAt(settings.createdAt());
        entity.setUpdatedAt(settings.updatedAt());
        entity.setVersion(settings.version());
        return entity;
    }
}
