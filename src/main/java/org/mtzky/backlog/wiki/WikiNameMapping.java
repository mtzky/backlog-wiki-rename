package org.mtzky.backlog.wiki;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class WikiNameMapping {

    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[([^]]*)]]");

    private final Map<String, String> mapping;

    public WikiNameMapping(final String... args) {
        final var length = args.length;
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Invalid argument length: " + length);
        }

        mapping = new HashMap<>();
        for (int i = 0, len = length / 2; i < len; i++) {
            mapping.put(args[i * 2], args[i * 2 + 1]);
        }
    }

    public boolean isEmpty() {
        return mapping.isEmpty();
    }

    public Optional<String> getNewName(final String nameToRename) {
        return Optional.ofNullable(mapping.get(nameToRename));
    }

    public String replaceAll(final String content) {
        final var matcher = WIKI_LINK_PATTERN.matcher(content);
        return matcher.replaceAll(result -> {
            final var newName = mapping.get(result.group(1));
            return newName != null ? ("[[" + newName + "]]") : result.group();
        });
    }

}
