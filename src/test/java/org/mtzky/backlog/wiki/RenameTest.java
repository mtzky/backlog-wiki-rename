package org.mtzky.backlog.wiki;

import com.nulabinc.backlog4j.Wiki;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class RenameTest {

    private static String[] args;

    @BeforeAll
    static void beforeAll() throws Throwable {
        final var classLoader = Optional
                .ofNullable(Thread.currentThread().getContextClassLoader())
                .orElseGet(ClassLoader::getSystemClassLoader);
        try (final var src = classLoader.getResourceAsStream("mapping.properties")) {
            assumeTrue(src != null, "No such file: src/test/resources/mapping.properties");

            final var properties = new Properties();
            try (final var reader = new InputStreamReader(src, UTF_8)) {
                properties.load(reader);
            }

            args = properties.entrySet().stream()
                    .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
                    .map(Object::toString)
                    .toArray(String[]::new);
        }
    }

    @Test
    void mainMethod() throws Throwable {
        Rename.main(args);
    }

    @Test
    @Disabled
    void listWikis() throws Throwable {
        final var output = new StringWriter();
        new AppBacklogClient(new AppConfig())
                .getWikis()
                .stream()
                .map(Wiki::getName)
                // .filter(n -> n.startsWith("foo/bar"))
                .collect(Properties::new, (p, name) -> {
                    final var renamedName = "baz/" + name;
                    p.setProperty(name, renamedName);
                }, Properties::putAll)
                .store(output, """
                         suppress inspection "UnusedProperty" for whole file
                         suppress inspection "NonAsciiCharacters" for whole file
                        """.stripTrailing());
        System.out.println(output);
    }

}