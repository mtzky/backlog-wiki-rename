package org.mtzky.backlog.wiki;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

import static java.lang.System.Logger.Level.INFO;
import static java.nio.charset.StandardCharsets.UTF_8;

public class WikiNameMapping {

    static final String MAPPING_FILE_KEY = "org.mtzky.backlog.wiki.mapping.file";
    static final Path MAPPING_FILE_PATH = Path.of(System.getProperty(
            MAPPING_FILE_KEY,
            "src/conf/mapping.properties"
    ));

    private static final System.Logger LOG = System.getLogger(WikiNameMapping.class.getName());
    private static final Pattern WIKI_LINK_PATTERN = Pattern.compile("\\[\\[([^]]*)]]");

    private final Map<String, String> mapping = new HashMap<>();

    public boolean isEmpty() {
        return mapping.isEmpty();
    }

    public Optional<String> getNewName(String nameToRename) {
        return Optional.ofNullable(mapping.get(nameToRename));
    }

    public String replaceAll(final String content) {
        final var matcher = WIKI_LINK_PATTERN.matcher(content);
        return matcher.replaceAll(result -> getNewName(result.group(1))
                .map("[[%s]]"::formatted)
                .orElseGet(result::group)
        );
    }

    WikiNameMapping file(final Path mappingFilePath) {
        if (Files.isRegularFile(mappingFilePath)) {
            LOG.log(INFO, "Loading mapping file: {0}", mappingFilePath);

            final var mapping = new Properties();
            try (final var src = Files.newBufferedReader(mappingFilePath, UTF_8)) {
                mapping.load(src);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }

            for (final var oldName : mapping.stringPropertyNames()) {
                final var newName = mapping.getProperty(oldName);
                if (oldName.equals(newName)) {
                    continue;
                }

                this.mapping.put(oldName, newName);
            }
        }

        return this;
    }

    WikiNameMapping args(final String... args) {
        final var length = args.length;
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Invalid argument length: " + length);
        }

        for (int i = 0, len = length / 2; i < len; i++) {
            final var oldName = args[i * 2];
            final var newName = args[i * 2 + 1];
            if (oldName.equals(newName)) {
                continue;
            }

            mapping.put(oldName, newName);
        }

        return this;
    }

}
