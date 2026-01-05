package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.Wiki;
import org.mtzky.backlog.wiki.client.AppBacklogClient;
import org.mtzky.backlog.wiki.config.AppEnvConfig;

import static java.lang.System.Logger.Level.WARNING;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;

public class CheckDeadLink implements Runnable {

    private static final System.Logger LOG = System.getLogger(CheckDeadLink.class.getName());

    private final AppEnvConfig config;
    private final AppBacklogClient client;

    public CheckDeadLink(final AppEnvConfig config, final AppBacklogClient client) {
        this.config = config;
        this.client = client;
    }

    static void main() {
        final var config = new AppEnvConfig();
        final var client = new AppBacklogClient(config);

        new CheckDeadLink(config, client).run();
    }

    @Override
    public void run() {
        final var urlPrefix = "https://%s.backlog.%s/alias/wiki".formatted(config.spaceId(), config.spaceType());

        final var wikis = client.getWikis();
        final var names = wikis.stream().map(Wiki::getName).collect(toSet());
        for (final var wiki : wikis) {
            final var id = wiki.getId();
            final var wikiDetail = client.getWiki(id);
            final var contentToCheck = wikiDetail.getContent();

            final var wikiParser = new WikiParser(contentToCheck);
            final var deadLinks = wikiParser.streamLinks()
                    .filter(not(names::contains))
                    .toList();

            if (deadLinks.isEmpty()) {
                continue;
            }

            LOG.log(
                    WARNING,
                    "Dead links found: wiki.url={0}/{1}, wiki.name={2}, deadLinks({3})={4}",
                    urlPrefix,
                    wiki.getIdAsString(),
                    wiki.getName(),
                    deadLinks.size(),
                    deadLinks
            );
        }
    }

}
