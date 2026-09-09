package com.mavora.agents.infrastructure.llm;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.agents.application.LlmRequest;
import com.mavora.agents.domain.AgentType;
import com.mavora.shared.domain.OrganizationId;
import org.junit.jupiter.api.Test;

class FakeLlmProviderTest {

    private final FakeLlmProvider provider = new FakeLlmProvider(new ObjectMapper());

    @Test
    void vapewaveCopiesSellOnlyViaDmOrWhatsAppAt15() {
        String content = provider.complete(new LlmRequest(
                OrganizationId.generate(),
                AgentType.INSTAGRAM,
                "fake",
                "sys",
                "Company: VapeWave\nEmpresa: VapeWave"
        )).content();
        assertThat(content).contains("WhatsApp");
        assertThat(content).contains("15");
        assertThat(content).contains("60K");
        assertThat(content).doesNotContain("enlace de la bio");
        assertThat(content).doesNotContain("65fls");
    }

    @Test
    void analyticsWithAlcanceAndSeguidoresDoesNotInventLinkedIn() {
        String content = provider.complete(new LlmRequest(
                OrganizationId.generate(),
                AgentType.ANALYST,
                "fake",
                "sys",
                "Snapshots:\nalcance=1200 @2026-09-09T10:00:00Z\nseguidores=340 @2026-09-09T10:00:00Z"
        )).content();
        assertThat(content).contains("Alcance y seguidores");
        assertThat(content).contains("WhatsApp");
        assertThat(content).doesNotContain("LinkedIn");
    }
}
