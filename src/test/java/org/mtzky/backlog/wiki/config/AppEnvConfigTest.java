package org.mtzky.backlog.wiki.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppEnvConfigTest {

    @Test
    void env() {
        final var config = new AppEnvConfig();

        assertNotNull(config.spaceType());
        assertNotNull(config.spaceId());
        assertNotNull(config.apiKey());
        assertNotNull(config.projectIdOrKey());
    }

}