package org.mtzky.backlog.wiki.config;

import java.util.Map;
import java.util.Optional;

import static java.util.function.Predicate.not;

public record AppEnvConfig(
        String spaceType,
        String spaceId,
        String apiKey,
        String projectIdOrKey
) implements AppConfig {

    private static final Map<String, String> ENV = System.getenv();

    public AppEnvConfig() {
        this(
                requireEnv("SPACE_TYPE"),
                requireEnv("SPACE_ID"),
                requireEnv("API_KEY"),
                requireEnv("PROJECT_ID_OR_KEY")
        );
    }

    private static String requireEnv(final String name) {
        return Optional.ofNullable(ENV.get(name))
                .filter(not(String::isBlank))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No such property: %s for the current system environment".formatted(name)
                ));
    }

}
