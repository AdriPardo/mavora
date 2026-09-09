package com.mavora.instagram.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InstagramSlotTest {

    @Test
    void publishesOnlyAfterClaim() {
        Instant now = Instant.parse("2026-09-07T12:00:00Z");
        InstagramSlot slot = InstagramSlot.schedule(
                OrganizationId.generate(),
                InstagramFormat.REEL,
                now,
                "Hook",
                "Caption",
                "CTA",
                List.of("#pymes"),
                List.of(UUID.randomUUID()),
                now
        );
        assertThatThrownBy(() -> slot.markPublished("ig_1", now)).isInstanceOf(DomainException.class);
        slot.claimForPublish();
        slot.markPublished("ig_1", now);
        assertThat(slot.status()).isEqualTo(InstagramSlotStatus.PUBLISHED);
        assertThat(slot.igMediaId()).isEqualTo("ig_1");
    }

    @Test
    void cancelOnlyAffectsScheduled() {
        Instant now = Instant.parse("2026-09-07T12:00:00Z");
        InstagramSlot slot = InstagramSlot.schedule(
                OrganizationId.generate(),
                InstagramFormat.STORY,
                now,
                "Hook",
                "Caption",
                "CTA",
                List.of(),
                List.of(UUID.randomUUID()),
                now
        );
        slot.cancel();
        assertThat(slot.status()).isEqualTo(InstagramSlotStatus.CANCELLED);
    }
}
