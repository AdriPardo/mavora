package com.mavora.instagram.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class InstagramPlaybookTest {

    @Test
    void withoutAutonomyUsesPlaybookTimeEvenForTheFirstSlot() {
        Instant mondayNoon = ZonedDateTime.of(2026, 9, 7, 12, 0, 0, 0, InstagramPlaybook.ZONE).toInstant();
        InstagramPlaybook.SlotBlueprint first = InstagramPlaybook.weekMix().getFirst();
        Instant scheduled = InstagramPlaybook.scheduledAt(mondayNoon, first, 0, false);
        ZonedDateTime when = scheduled.atZone(InstagramPlaybook.ZONE);
        assertThat(when.toLocalDate()).isEqualTo(LocalDate.of(2026, 9, 14));
        assertThat(when.toLocalTime()).isEqualTo(LocalTime.of(8, 15));
    }

    @Test
    void withAutonomyTheFirstSlotPublishesImmediately() {
        Instant mondayNoon = ZonedDateTime.of(2026, 9, 7, 12, 0, 0, 0, InstagramPlaybook.ZONE).toInstant();
        InstagramPlaybook.SlotBlueprint first = InstagramPlaybook.weekMix().getFirst();
        assertThat(InstagramPlaybook.scheduledAt(mondayNoon, first, 0, true)).isEqualTo(mondayNoon);
    }
}
