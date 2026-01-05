package org.mtzky.backlog.wiki;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikiNameMappingTest {

    private final WikiNameMapping sut = new WikiNameMapping();

    @Nested
    class args {

        @ParameterizedTest
        @CsvSource({
                "k1, v1",
                "k2, v2"
        })
        void matched(final String key, final String expectedValue) {
            sut.args("k1", "v1", "k2", "v2", "k3", "v3");

            final var actual = sut.getNewName(key);

            assertAll(
                    () -> assertFalse(sut.isEmpty()),
                    () -> assertEquals(expectedValue, actual.orElseThrow())
            );
        }

        @ParameterizedTest
        @CsvSource({
                "K2",
                "k4",
        })
        void notMatched(final String key) {
            sut.args("k1", "v1", "k2", "v2", "k3", "v3");

            final var actual = sut.getNewName(key);

            assertAll(
                    () -> assertFalse(sut.isEmpty()),
                    () -> assertFalse(actual.isPresent())
            );
        }

        @Test
        void sameKey() {
            sut.args("same-key", "1st-value", "same-key", "2nd-value");

            final var actual = sut.getNewName("same-key");

            assertAll(
                    () -> assertFalse(sut.isEmpty()),
                    () -> assertEquals("2nd-value", actual.orElseThrow())
            );
        }

        @Test
        void sameValue() {
            sut.args("same-kv", "same-kv");

            final var actual = sut.getNewName("same-kv");

            assertAll(
                    () -> assertTrue(sut.isEmpty()),
                    () -> assertFalse(actual.isPresent())
            );
        }

        @Test
        void empty() {
            sut.args();

            final var actual = sut.getNewName(null);

            assertAll(
                    () -> assertTrue(sut.isEmpty()),
                    () -> assertFalse(actual.isPresent())
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3, 5})
        void invalidArgumentLength(final int argsLength) {
            final var args = IntStream.range(0, argsLength).mapToObj(Integer::toString).toArray(String[]::new);

            final var actual = assertThrows(IllegalArgumentException.class, () -> sut.args(args));

            assertEquals("Invalid argument length: " + argsLength, actual.getMessage());
        }

    }

    @Nested
    class file {

        @Test
        void properties() throws Throwable {
            final var mappingFileName = "WikiNameMappingTest.properties";
            final var mappingFileUri = requireNonNull(WikiNameMappingTest.class.getResource(mappingFileName)).toURI();
            final var mappingFilePath = Path.of(mappingFileUri);

            sut.file(mappingFilePath);

            assertAll(
                    () -> assertFalse(sut.isEmpty()),
                    () -> assertEquals("bar", sut.getNewName("foo").orElseThrow()),
                    () -> assertFalse(sut.getNewName("baz").isPresent()),
                    () -> assertFalse(sut.getNewName("qux").isPresent())
            );
        }

        @Test
        void noSuchFile() {
            sut.file(Path.of("no-such-file.properties"));

            assertTrue(sut.isEmpty());
        }

    }

}