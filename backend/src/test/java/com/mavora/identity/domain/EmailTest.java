package com.mavora.identity.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void normalizesCaseAndTrim() {
        assertThat(new Email("  Ada@Example.COM ").normalized()).isEqualTo("ada@example.com");
    }

    @Test
    void rejectsInvalid() {
        assertThatThrownBy(() -> new Email("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
