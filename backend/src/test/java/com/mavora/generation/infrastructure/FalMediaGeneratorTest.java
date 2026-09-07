package com.mavora.generation.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavora.shared.domain.DomainException;
import org.junit.jupiter.api.Test;

class FalMediaGeneratorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void extractsImageAndVideoUrls() throws Exception {
        assertThat(FalMediaGenerator.extractFileUrl(objectMapper.readTree(
                "{\"images\":[{\"url\":\"https://fal.media/a.jpg\"}]}"
        ))).isEqualTo("https://fal.media/a.jpg");
        assertThat(FalMediaGenerator.extractFileUrl(objectMapper.readTree(
                "{\"video\":{\"url\":\"https://fal.media/a.mp4\"}}"
        ))).isEqualTo("https://fal.media/a.mp4");
    }

    @Test
    void rejectsEmptyPayload() {
        assertThatThrownBy(() -> FalMediaGenerator.extractFileUrl(objectMapper.createObjectNode()))
                .isInstanceOf(DomainException.class);
    }
}
