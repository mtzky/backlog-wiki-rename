package org.mtzky.backlog.wiki.config;

import com.nulabinc.backlog4j.conf.BacklogComConfigure;
import com.nulabinc.backlog4j.conf.BacklogConfigure;
import com.nulabinc.backlog4j.conf.BacklogJpConfigure;
import com.nulabinc.backlog4j.conf.BacklogToolConfigure;

public interface AppConfig {

    default BacklogConfigure configure() {
        final var spaceKey = spaceId();
        final var spaceType = spaceType();
        return (switch (spaceType.toUpperCase()) {
            case "COM" -> new BacklogComConfigure(spaceKey);
            case "JP" -> new BacklogJpConfigure(spaceKey);
            case "TOOL" -> new BacklogToolConfigure(spaceKey);
            default -> throw new IllegalArgumentException("unknown space type: " + spaceType);
        }).apiKey(apiKey());
    }

    String spaceType();

    String spaceId();

    String apiKey();

    String projectIdOrKey();

}
