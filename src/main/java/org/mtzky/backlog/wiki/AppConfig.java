package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.conf.BacklogComConfigure;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;
import com.nulabinc.backlog4j.conf.BacklogToolConfigure;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.function.Function;

public class AppConfig {

    private final SpaceType spaceType;
    private final String spaceId;
    private final String apiKey;
    private final String projectIdOrKey;

    public AppConfig() {
        final var env = new SystemEnv();
        spaceType = env.get("SPACE_TYPE", SpaceType::of);
        spaceId = env.get("SPACE_ID");
        apiKey = env.get("API_KEY");
        projectIdOrKey = env.get("PROJECT_ID_OR_KEY");
    }

    public SpaceType getSpaceType() {
        return spaceType;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getProjectIdOrKey() {
        return projectIdOrKey;
    }

    @SuppressWarnings("unused")
    public enum SpaceType {

        COM(BacklogComConfigure::new),
        JP(BacklogJpConfigure::new),
        TOOL(BacklogToolConfigure::new);

        private final BacklogConfigureFactory factory;

        SpaceType(final BacklogConfigureFactory factory) {
            this.factory = factory;
        }

        static SpaceType of(final String type) {
            return valueOf(type.toUpperCase());
        }

        public BacklogConfigure newBacklogConfigure(final String spaceKey) throws MalformedURLException {
            return factory.apply(spaceKey);
        }

        @FunctionalInterface
        private interface BacklogConfigureFactory {
            BacklogConfigure apply(String spaceKey) throws MalformedURLException;
        }

    }

    private static class SystemEnv {

        private final Map<String, String> env = System.getenv();

        String get(final String name) {
            final var v = env.get(name);
            if (v == null) {
                throw new IllegalArgumentException(String.format(
                        "No such property: %s for the current system environment", name));
            }
            return v;
        }

        @SuppressWarnings("SameParameterValue")
        <T> T get(final String name, final Function<String, T> func) {
            return func.apply(get(name));
        }

    }

}
