package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.api.option.UpdateWikiParams;
import com.nulabinc.backlog4j.http.NameValuePair;
import org.mtzky.backlog.wiki.client.AppBacklogClient;
import org.mtzky.backlog.wiki.config.AppEnvConfig;

import java.util.Arrays;
import java.util.Properties;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.toSet;
import static org.mtzky.backlog.wiki.WikiNameMapping.MAPPING_FILE_KEY;
import static org.mtzky.backlog.wiki.WikiNameMapping.MAPPING_FILE_PATH;

public class Rename implements Runnable {

    private static final System.Logger LOG = System.getLogger(Rename.class.getName());

    private final AppBacklogClient client;
    private final WikiNameMapping mapping;

    public Rename(final AppBacklogClient client, final WikiNameMapping mapping) {
        this.client = client;
        this.mapping = mapping;
    }

    public static void main(final String... args) throws Throwable {
        final var config = new AppEnvConfig();
        final var client = new AppBacklogClient(config);

        final var mapping = new WikiNameMapping()
                .file(MAPPING_FILE_PATH)
                .args(args);
        if (mapping.isEmpty()) {
            LOG.log(
                    WARNING,
                    "No mapping found. args={0}, {1}={2}",
                    Arrays.toString(args),
                    MAPPING_FILE_KEY,
                    MAPPING_FILE_PATH
            );

            final var mappingProperties = new Properties();
            for (final var wiki : client.getWikis()) {
                final var name = wiki.getName();
                mappingProperties.put(name, name);
            }

            try (final var w = newBufferedWriter(MAPPING_FILE_PATH, UTF_8, CREATE)) {
                mappingProperties.store(w, """
                        suppress inspection "NonAsciiCharacters" for whole file
                        """.stripTrailing());
            }

            return;
        }

        new Rename(client, mapping).run();
    }

    @Override
    public void run() {
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
