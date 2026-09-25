package org.mtzky.backlog.wiki;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public record WikiParser(String content) {

    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[([^]]*)]]");

    public String migrateLinks(final WikiNameMapping mapping) {
        final var matcher = WIKI_LINK_PATTERN.matcher(content);
        return matcher.replaceAll(result -> {
            final var s = mapping.getNewName(result.group(1))
                    .map("[[%s]]"::formatted)
                    .orElseGet(result::group);
            return Matcher.quoteReplacement(s);
        });
    }

    public Stream<String> streamLinks() {
        final var matcher = WIKI_LINK_PATTERN.matcher(content);
        return matcher.results().map(m -> m.group(1));
    }

}
