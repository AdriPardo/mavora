package com.mavora.instagram.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MediaAssetTest {

    @Test
    void generatedFalPrefixIsNotOwnerUpload() {
        Instant now = Instant.parse("2026-09-09T12:00:00Z");
        MediaAsset generated = MediaAsset.create(
                OrganizationId.generate(), null, MediaKind.IMAGE, "fal-image.jpg",
                "image/jpeg", "path/fal.jpg", 12, "prompt", now
        );
        MediaAsset owned = MediaAsset.create(
                OrganizationId.generate(), null, MediaKind.IMAGE, "logo-60k.png",
                "image/png", "path/logo.png", 12, "logo", now
        );
        assertThat(generated.isGenerated()).isTrue();
        assertThat(owned.isGenerated()).isFalse();
    }
}
