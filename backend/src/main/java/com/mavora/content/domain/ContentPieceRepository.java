package com.mavora.content.domain;

import com.mavora.shared.domain.OrganizationId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContentPieceRepository {

    ContentPiece save(ContentPiece piece);

    Optional<ContentPiece> findById(UUID id, OrganizationId organizationId);

    List<ContentPiece> findByOrganization(OrganizationId organizationId);

    Optional<ContentPiece> findLatestApproved(OrganizationId organizationId);
}
