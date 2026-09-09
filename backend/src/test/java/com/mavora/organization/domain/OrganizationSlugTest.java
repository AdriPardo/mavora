package com.mavora.organization.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrganizationSlugTest {

    @Test
    void slugsFromReadableNames() {
        assertThat(OrganizationSlug.fromName("Acme SaaS")).isEqualTo("acme-saas");
        assertThat(OrganizationSlug.fromName("  España 2026 ")).isEqualTo("espana-2026");
    }

    @Test
    void fallsBackWhenEmpty() {
        assertThat(OrganizationSlug.fromName("@@@")).isEqualTo("org");
    }
}
