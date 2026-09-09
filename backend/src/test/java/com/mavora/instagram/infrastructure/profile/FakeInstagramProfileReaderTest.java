package com.mavora.instagram.infrastructure.profile;

import static org.assertj.core.api.Assertions.assertThat;

import com.mavora.instagram.application.InstagramProfileSnapshot;
import com.mavora.instagram.domain.InstagramAccount;
import com.mavora.instagram.domain.InstagramProvider;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class FakeInstagramProfileReaderTest {

    private final FakeInstagramProfileReader reader = new FakeInstagramProfileReader();

    @Test
    void vapewaveProfileHasNoInventedMetricsOrWebsite() {
        InstagramAccount account = InstagramAccount.connect(
                OrganizationId.generate(),
                InstagramProvider.FAKE,
                "fake-ig",
                "vapewave.vlc",
                null,
                "cipher",
                Instant.parse("2027-01-01T00:00:00Z"),
                Instant.parse("2026-09-09T12:00:00Z")
        );
        InstagramProfileSnapshot snapshot = reader.read(account, "token", "v21.0");
        assertThat(snapshot.username()).isEqualTo("vapewave.vlc");
        assertThat(snapshot.name()).isEqualTo("VapeWave");
        assertThat(snapshot.biography()).contains("Pedidos por DM");
        assertThat(snapshot.website()).isNull();
        assertThat(snapshot.followers()).isNull();
        assertThat(snapshot.recentCaptions()).isNotEmpty();
        assertThat(snapshot.recentCaptions().toString()).doesNotContain("presupuesto");
        assertThat(snapshot.pageAbout()).contains("VapeWave");
        assertThat(snapshot.pageAbout()).contains("WhatsApp");
        assertThat(snapshot.pageAbout()).contains("15");
        assertThat(snapshot.followers()).isNull();
    }
}
