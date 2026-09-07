package com.mavora.company.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.mavora.shared.domain.OrganizationId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CompanyTest {

    @Test
    void fillBlanksDoesNotOverwriteExistingValues() {
        Instant now = Instant.parse("2026-09-07T19:00:00Z");
        Company company = Company.create(
                OrganizationId.generate(),
                "Ig Analytics",
                "https://already.example",
                "Descripción hecha a mano",
                null,
                now
        );
        assertThat(company.fillBlanks(
                "https://instagram.example",
                "Bio de Instagram",
                "SaaS B2B",
                now.plusSeconds(10)
        )).containsExactly("company.market");
        assertThat(company.websiteUrl()).isEqualTo("https://already.example");
        assertThat(company.description()).isEqualTo("Descripción hecha a mano");
        assertThat(company.market()).isEqualTo("SaaS B2B");
        assertThat(company.name()).isEqualTo("Ig Analytics");
    }
}
