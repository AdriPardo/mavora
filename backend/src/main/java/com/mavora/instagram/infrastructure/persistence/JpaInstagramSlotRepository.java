package com.mavora.instagram.infrastructure.persistence;

import com.mavora.instagram.domain.InstagramSlot;
import com.mavora.instagram.domain.InstagramSlotRepository;
import com.mavora.instagram.domain.InstagramSlotStatus;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class JpaInstagramSlotRepository implements InstagramSlotRepository {

    private final InstagramSlotJpaRepository jpaRepository;
    private final JsonListMapper jsonListMapper;

    public JpaInstagramSlotRepository(InstagramSlotJpaRepository jpaRepository, JsonListMapper jsonListMapper) {
        this.jpaRepository = jpaRepository;
        this.jsonListMapper = jsonListMapper;
    }

    @Override
    public InstagramSlot save(InstagramSlot slot) {
        InstagramSlotEntity entity = jpaRepository.findById(slot.id()).orElseGet(InstagramSlotEntity::new);
        boolean creating = entity.getId() == null;
        copy(slot, entity);
        entity.markNew(creating);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<InstagramSlot> findById(UUID id, OrganizationId organizationId) {
        return jpaRepository.findByIdAndOrganizationId(id, organizationId.value()).map(this::toDomain);
    }

    @Override
    public List<InstagramSlot> findByOrganization(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdOrderByScheduledAtAsc(organizationId.value()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<InstagramSlot> findScheduled(OrganizationId organizationId) {
        return jpaRepository.findByOrganizationIdAndStatusOrderByScheduledAtAsc(
                        organizationId.value(), InstagramSlotStatus.SCHEDULED
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<InstagramSlot> lockNextDue(Instant now) {
        return jpaRepository.lockDue(now, PageRequest.of(0, 1)).stream().findFirst().map(entity -> {
            entity.markNew(false);
            return toDomain(entity);
        });
    }

    private InstagramSlot toDomain(InstagramSlotEntity entity) {
        return InstagramSlot.reconstitute(
                entity.getId(),
                new OrganizationId(entity.getOrganizationId()),
                entity.getFormat(),
                entity.getStatus(),
                entity.getScheduledAt(),
                entity.getHook(),
                entity.getCaption(),
                entity.getCta(),
                jsonListMapper.readStrings(entity.getHashtagsJson()),
                jsonListMapper.readUuids(entity.getMediaAssetIdsJson()),
                entity.getIgMediaId(),
                entity.getErrorMessage(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                entity.getVersion()
        );
    }

    private void copy(InstagramSlot slot, InstagramSlotEntity entity) {
        entity.setId(slot.id());
        entity.setOrganizationId(slot.organizationId().value());
        entity.setFormat(slot.format());
        entity.setStatus(slot.status());
        entity.setScheduledAt(slot.scheduledAt());
        entity.setHook(slot.hook());
        entity.setCaption(slot.caption());
        entity.setCta(slot.cta());
        entity.setHashtagsJson(jsonListMapper.strings(slot.hashtags()));
        entity.setMediaAssetIdsJson(jsonListMapper.uuids(slot.mediaAssetIds()));
        entity.setIgMediaId(slot.igMediaId());
        entity.setErrorMessage(slot.errorMessage());
        entity.setPublishedAt(slot.publishedAt());
        entity.setCreatedAt(slot.createdAt());
        entity.setVersion(slot.version());
    }
}
