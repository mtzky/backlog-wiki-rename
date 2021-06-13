package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.BacklogAPIException;
import com.nulabinc.backlog4j.BacklogClientFactory;
import com.nulabinc.backlog4j.Wiki;
import com.nulabinc.backlog4j.api.option.UpdateWikiParams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AppBacklogClientTest {

    private static final AppConfig CONFIG = new AppConfig();
    private static long projectId;

    private AppBacklogClient client;

    @BeforeAll
    static void beforeAll() throws Throwable {
        final var configure = CONFIG.getSpaceType()
                .newBacklogConfigure(CONFIG.getSpaceId())
                .apiKey(CONFIG.getApiKey());
        projectId = new BacklogClientFactory(configure)
                .newClient()
                .getProject(CONFIG.getProjectIdOrKey())
                .getId();
    }

    @BeforeEach
    void setUp() throws Throwable {
        client = new AppBacklogClient(CONFIG);
    }

    @Test
    void getWikis() {
        final var wikis = client.getWikis();
        assumeFalse(wikis.isEmpty());

        assertTrue(wikis.stream().allMatch(wiki -> wiki.getProjectId() == projectId));
    }

    @Test
    void getWiki() {
        final var firstWiki = client.getWikis().stream().findFirst().map(Wiki::getProjectId);
        assumeTrue(firstWiki.isPresent());

        assertEquals(projectId, firstWiki.get());
    }

    @Test
    void updateWiki() {
        final var firstWiki = client.getWikis().stream().findFirst().map(Wiki::getId);
        assumeTrue(firstWiki.isPresent());
        final var wikiId = firstWiki.get();
        final var params = new UpdateWikiParams(wikiId);

        final var actual = assertThrows(BacklogAPIException.class, () -> client.updateWiki(params));

        assertEquals(400, actual.getStatusCode());
    }

}