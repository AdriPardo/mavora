package com.mavora.instagram.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mavora.shared.domain.DomainException;
import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstagramAccountTest {

    @Test
    void connectsWithAutonomyAndStripsAtSign() {
        Instant now = Instant.parse("2026-09-07T12:00:00Z");
        InstagramAccount account = InstagramAccount.connect(
                OrganizationId.generate(),
                InstagramProvider.FAKE,
                "ig-1",
                "@acme.demo",
                null,
                "cipher",
                now.plusSeconds(3600),
                now
        );
        assertThat(account.username()).isEqualTo("acme.demo");
        assertThat(account.canAutoPublish()).isTrue();
        account.setAutonomy(false);
        assertThat(account.canAutoPublish()).isFalse();
        account.disconnect(now.plusSeconds(10));
        assertThat(account.isConnected()).isFalse();
        assertThat(account.tokenCiphertext()).isNull();
        assertThatThrownBy(() -> account.setAutonomy(true)).isInstanceOf(DomainException.class);
    }
}
