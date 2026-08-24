package com.mavora.company.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HttpUrlTest {

    @Test
    void acceptsHttpAndHttpsWithoutCredentials() {
        assertThat(HttpUrl.normalize("https://example.com/path")).startsWith("https://example.com");
        assertThat(HttpUrl.normalizeOptional("")).isEmpty();
    }

    @Test
    void rejectsNonHttpAndUserInfo() {
        assertThatThrownBy(() -> HttpUrl.normalize("ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HttpUrl.normalize("https://user:pass@example.com"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HttpUrl.normalize("not-a-url"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
