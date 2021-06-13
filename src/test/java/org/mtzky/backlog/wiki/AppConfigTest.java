package org.mtzky.backlog.wiki;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void env() {
        final var config = new AppConfig();

        assertNotNull(config.getSpaceType());
        assertNotNull(config.getSpaceId());
        assertNotNull(config.getApiKey());
        assertNotNull(config.getProjectIdOrKey());
    }

}