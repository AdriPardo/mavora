package com.mavora.instagram.api;

import com.mavora.instagram.application.MediaLibraryService;
import com.mavora.instagram.domain.MediaAsset;
import com.mavora.instagram.domain.MediaAssetRepository;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MediaFileController {

    private final MediaLibraryService mediaLibraryService;
    private final MediaAssetRepository mediaAssetRepository;

    public MediaFileController(MediaLibraryService mediaLibraryService, MediaAssetRepository mediaAssetRepository) {
        this.mediaLibraryService = mediaLibraryService;
        this.mediaAssetRepository = mediaAssetRepository;
    }

    @GetMapping("/api/v1/media/{assetId}/file")
    public ResponseEntity<byte[]> file(
            @PathVariable UUID assetId,
            @RequestParam("exp") long exp,
            @RequestParam("sig") String sig
    ) {
        byte[] bytes = mediaLibraryService.readPublic(assetId, exp, sig);
        String contentType = mediaAssetRepository.findById(assetId)
                .map(MediaAsset::contentType)
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes);
    }
}
