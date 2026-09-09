package com.mavora.instagram.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class MetaConnectionSettingsTest {

    @Test
    void isReadyOnlyWhenAppIdAndSecretArePresent() {
        Instant now = Instant.parse("2026-09-07T18:00:00Z");
        MetaConnectionSettings settings = MetaConnectionSettings.create(
                OrganizationId.generate(), "1234567890", null, null, "v21.0", now
        );
        assertThat(settings.isReady()).isFalse();
        settings.update("1234567890", "cipher-secret", null, "v22.0", now.plusSeconds(10));
        assertThat(settings.isReady()).isTrue();
        assertThat(settings.graphVersion()).isEqualTo("v22.0");
        settings.clearSecret(now.plusSeconds(20));
        assertThat(settings.isReady()).isFalse();
    }

    @Test
    void rejectsInvalidGraphVersion() {
        Instant now = Instant.parse("2026-09-07T18:00:00Z");
        assertThatThrownBy(() -> MetaConnectionSettings.create(
                OrganizationId.generate(), "1234567890", "cipher", null, "twenty-one", now
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
