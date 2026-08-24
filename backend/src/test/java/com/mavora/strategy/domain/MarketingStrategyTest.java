package com.mavora.strategy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MarketingStrategyTest {

    @Test
    void draftCanBeApprovedOrRejectedOnce() {
        Instant now = Instant.parse("2026-08-24T12:00:00Z");
        MarketingStrategy strategy = MarketingStrategy.draft(
                OrganizationId.generate(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Posicionamiento",
                "ICP",
                "[\"linkedin\"]",
                "[\"educacion\"]",
                "[{\"name\":\"signups\"}]",
                "Narrativa",
                now
        );
        strategy.approve(now);
        assertThat(strategy.status()).isEqualTo(StrategyStatus.APPROVED);
        assertThatThrownBy(() -> strategy.reject(now)).isInstanceOf(DomainException.class);
    }
}
