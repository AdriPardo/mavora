package com.mavora.content.domain;

import java.util.List;
import java.util.UUID;

public interface ContentVariantRepository {

    ContentVariant save(ContentVariant variant);

    List<ContentVariant> findByPiece(UUID pieceId);
}
