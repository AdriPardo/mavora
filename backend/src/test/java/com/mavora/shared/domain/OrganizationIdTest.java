package com.mavora.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrganizationIdTest {

    @Test
    void generateProducesDistinctIds() {
        assertThat(OrganizationId.generate()).isNotEqualTo(OrganizationId.generate());
    }

    @Test
    void fromParsesUuid() {
        OrganizationId id = OrganizationId.generate();
        assertThat(OrganizationId.from(id.toString())).isEqualTo(id);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new OrganizationId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
