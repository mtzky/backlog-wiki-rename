package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.Wiki;
import com.nulabinc.backlog4j.api.option.UpdateWikiParams;
import com.nulabinc.backlog4j.http.NameValuePair;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class Rename implements Runnable {

    private static final System.Logger LOG = System.getLogger(Rename.class.getName());

    private final AppBacklogClient client;
    private final WikiNameMapping mapping;

    public Rename(final AppBacklogClient client, final WikiNameMapping mapping) {
        this.client = client;
        this.mapping = mapping;
    }

    public static void main(final String... args) throws Exception {
        final var config = new AppConfig();
        final var client = new AppBacklogClient(config);
        final var mapping = new WikiNameMapping(args);

        new Rename(client, mapping).run();
    }

    @Override
    public void run() {
        if (mapping.isEmpty()) {
            final var delimiter = System.lineSeparator();
            final var wikiNames = client.getWikis().stream()
                    .map(Wiki::getName)
                    .collect(joining(delimiter));
            LOG.log(INFO, "list of Wiki names:{0}{1}", delimiter, wikiNames);
            return;
        }

        for (final var wiki : client.getWikis()) {
            final var id = wiki.getId();
            final var oldName = wiki.getName();
            LOG.log(DEBUG, "Wiki[id={0,number,#}, name={1}]", id, oldName);

            final var params = new UpdateWikiParams(id);
            mapping.getNewName(oldName).ifPresent(params::name);

            final var wikiDetail = client.getWiki(id);
            final var oldContent = wikiDetail.getContent();
            final var newContent = mapping.replaceAll(oldContent);
            if (!newContent.equals(oldContent)) {
                params.content(newContent);
            }

            final var paramList = params.getParamList();
            if (paramList.isEmpty()) {
                continue;
            }

            client.updateWiki(params);

            final var paramNames = paramList.stream().map(NameValuePair::getName).collect(toSet());
            LOG.log(INFO, "Wiki[id={0,number,#}] updated. params={1}", id, paramNames);
        }
    }

}
