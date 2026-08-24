package com.mavora.content.infrastructure.persistence;
import java.util.List; import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ContentVariantJpaRepository extends JpaRepository<ContentVariantEntity, UUID> {
    List<ContentVariantEntity> findByPieceIdOrderByCreatedAtAsc(UUID pieceId);
}
