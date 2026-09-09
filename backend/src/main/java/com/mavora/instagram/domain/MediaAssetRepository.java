package com.mavora.instagram.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository {

    MediaAsset save(MediaAsset asset);

    Optional<MediaAsset> findById(UUID id);

    Optional<MediaAsset> findById(UUID id, OrganizationId organizationId);

    List<MediaAsset> findByOrganization(OrganizationId organizationId);

    void delete(UUID id, OrganizationId organizationId);
}
