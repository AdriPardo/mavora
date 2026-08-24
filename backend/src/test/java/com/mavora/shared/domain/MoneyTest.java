package com.mavora.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void eurosAreEuroCents() {
        Money budget = Money.euros(50_000);
        assertThat(budget.amountCents()).isEqualTo(50_000);
        assertThat(budget.currency().getCurrencyCode()).isEqualTo("EUR");
        assertThat(budget.isZero()).isFalse();
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> Money.euros(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void plusRequiresSameCurrency() {
        Money euros = Money.euros(100);
        Money dollars = Money.ofCents(100, "USD");
        assertThatThrownBy(() -> euros.plus(dollars))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currency mismatch");
    }

    @Test
    void plusAddsCents() {
        assertThat(Money.euros(100).plus(Money.euros(50)).amountCents()).isEqualTo(150);
    }
}
