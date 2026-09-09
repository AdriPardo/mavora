package com.mavora.instagram.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MetaConnectionScopesTest {

    @Test
    void oauthScopesMatchUsedGraphCalls() {
        assertThat(MetaConnectionService.REQUIRED_SCOPES).containsExactly(
                "instagram_basic",
                "instagram_content_publish",
                "pages_show_list",
                "pages_read_engagement"
        );
    }
}
