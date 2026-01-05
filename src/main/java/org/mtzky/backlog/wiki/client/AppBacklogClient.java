package org.mtzky.backlog.wiki.client;

import com.nulabinc.backlog4j.BacklogClient;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.Wiki;
import com.nulabinc.backlog4j.api.option.UpdateWikiParams;
import org.mtzky.backlog.wiki.config.AppConfig;

import java.util.List;

public class AppBacklogClient {

    private final String projectIdOrKey;
    private final BacklogClient client;

    public AppBacklogClient(final AppConfig config) {
        projectIdOrKey = config.projectIdOrKey();
        final var configure = config.configure();
        final var httpClient = new RetryingBacklogHttpClient();
        client = new BacklogClientFactory(configure, httpClient).newClient();
    }

    public List<Wiki> getWikis() {
        return client.getWikis(projectIdOrKey);
    }

    public Wiki getWiki(final long id) {
        return client.getWiki(id);
    }

    public Wiki updateWiki(final UpdateWikiParams params) {
        return client.updateWiki(params);
    }

}
