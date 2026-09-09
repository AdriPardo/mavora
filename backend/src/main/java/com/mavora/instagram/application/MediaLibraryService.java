package com.mavora.instagram.application;

import com.mavora.instagram.domain.MediaAsset;
import com.mavora.instagram.domain.MediaAssetRepository;
import com.mavora.instagram.domain.MediaKind;
import com.mavora.organization.application.OrganizationAuthorizationService;
import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import com.mavora.shared.domain.ResourceNotFoundException;
import com.mavora.shared.domain.UserId;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MediaLibraryService {

    private static final Set<String> IMAGES = Set.of("image/jpeg", "image/jpg", "image/png");
    private static final Set<String> VIDEOS = Set.of("video/mp4");

    private final OrganizationAuthorizationService authorizationService;
    private final MediaAssetRepository assetRepository;
    private final MediaStorage mediaStorage;
    private final MediaUrlSigner mediaUrlSigner;
    private final Clock clock;
    private final long maxBytes;

    public MediaLibraryService(
            OrganizationAuthorizationService authorizationService,
            MediaAssetRepository assetRepository,
            MediaStorage mediaStorage,
            MediaUrlSigner mediaUrlSigner,
            Clock clock,
            @Value("${mavora.media.max-bytes:8388608}") long maxBytes
    ) {
        this.authorizationService = authorizationService;
        this.assetRepository = assetRepository;
        this.mediaStorage = mediaStorage;
        this.mediaUrlSigner = mediaUrlSigner;
        this.clock = clock;
        this.maxBytes = maxBytes;
    }

    @Transactional(readOnly = true)
    public List<MediaView> list(OrganizationId organizationId, UserId userId) {
        authorizationService.requireMember(organizationId, userId);
        return assetRepository.findByOrganization(organizationId).stream().map(this::toView).toList();
    }

    @Transactional
    public MediaView upload(
            OrganizationId organizationId,
            UserId userId,
            String filename,
            String contentType,
            byte[] bytes,
            String captionHint,
            UUID productId
    ) {
        authorizationService.requireWriter(organizationId, userId);
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("file is required");
        }
        if (bytes.length > maxBytes) {
            throw new DomainException("The file exceeds the maximum size of " + maxBytes + " bytes");
        }
        String type = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT);
        MediaKind kind;
        if (IMAGES.contains(type)) {
            kind = MediaKind.IMAGE;
        } else if (VIDEOS.contains(type)) {
            kind = MediaKind.VIDEO;
        } else {
            throw new DomainException("Only JPEG, PNG and MP4 files are accepted");
        }
        UUID assetId = UUID.randomUUID();
        String safeName = sanitize(filename);
        MediaStorage.StoredFile stored = mediaStorage.store(organizationId, assetId, safeName, type, bytes);
        MediaAsset asset = MediaAsset.reconstitute(
                assetId,
                organizationId,
                productId,
                kind,
                safeName,
                type,
                stored.storagePath(),
                stored.byteSize(),
                captionHint,
                clock.instant(),
                0
        );
        return toView(assetRepository.save(asset));
    }

    @Transactional
    public MediaAsset storeGenerated(
            OrganizationId organizationId,
            MediaKind kind,
            String filename,
            String contentType,
            byte[] bytes,
            String captionHint
    ) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("generated file is empty");
        }
        long limit = kind == MediaKind.VIDEO ? Math.max(maxBytes, 32_000_000L) : maxBytes;
        if (bytes.length > limit) {
            throw new DomainException("The generated file exceeds the maximum size");
        }
        UUID assetId = UUID.randomUUID();
        String safeName = sanitize(filename);
        MediaStorage.StoredFile stored = mediaStorage.store(organizationId, assetId, safeName, contentType, bytes);
        MediaAsset asset = MediaAsset.reconstitute(
                assetId,
                organizationId,
                null,
                kind,
                safeName,
                contentType,
                stored.storagePath(),
                stored.byteSize(),
                captionHint,
                clock.instant(),
                0
        );
        return assetRepository.save(asset);
    }

    @Transactional
    public void delete(OrganizationId organizationId, UserId userId, UUID assetId) {
        authorizationService.requireWriter(organizationId, userId);
        MediaAsset asset = assetRepository.findById(assetId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
        mediaStorage.delete(asset.storagePath());
        assetRepository.delete(assetId, organizationId);
    }

    public byte[] readPublic(UUID assetId, long exp, String signature) {
        if (!mediaUrlSigner.verify(assetId, exp, signature)) {
            throw new DomainException(
                    DomainException.ErrorType.FORBIDDEN, "Invalid media signature", "The media link is invalid or expired"
            );
        }
        MediaAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
        return mediaStorage.read(asset.storagePath());
    }

    public MediaAsset require(UUID assetId, OrganizationId organizationId) {
        return assetRepository.findById(assetId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
    }

    public MediaView toView(MediaAsset asset) {
        MediaUrlSigner.SignedUrl signed = mediaUrlSigner.sign(asset.id());
        return new MediaView(
                asset.id(),
                asset.kind().name(),
                asset.filename(),
                asset.contentType(),
                asset.byteSize(),
                asset.captionHint(),
                asset.productId(),
                signed.url(),
                asset.createdAt()
        );
    }

    private static String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "upload.bin";
        }
        String name = filename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        return name.isBlank() ? "upload.bin" : name;
    }

    public record MediaView(
            UUID id,
            String kind,
            String filename,
            String contentType,
            long byteSize,
            String captionHint,
            UUID productId,
            String url,
            java.time.Instant createdAt
    ) {
    }
}
