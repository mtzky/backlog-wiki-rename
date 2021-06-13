package org.mtzky.backlog.wiki;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WikiNameMappingTest {

    @ParameterizedTest
    @CsvSource({
            "foo [[bar]] bar [[bar/baz]] qux, foo [[BAR]] bar [[bar/baz]] qux"
    })
    void replaceAll(final String input, final String expected) {
        final var mapping = new WikiNameMapping("bar", "BAR");

        final var actual = mapping.replaceAll(input);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "k1, true, v1",
            "k2, true, v2",
            "K2, false, ",
            "k4, false, ",
    })
    void getNewName(final String key, final boolean present, final String expectedValue) {
        final var mapping = new WikiNameMapping("k1", "v1", "k2", "v2", "k3", "v3");
        final var actual = mapping.getNewName(key);

        assertFalse(mapping.isEmpty());
        assertEquals(present, actual.isPresent());
        assertEquals(expectedValue, actual.orElse(null));
    }

    @Test
    void sameKey() {
        final var mapping = new WikiNameMapping("same-key", "1st-value", "same-key", "2nd-value");
        final var actual = mapping.getNewName("same-key");

        assertFalse(mapping.isEmpty());
        assertTrue(actual.isPresent());
        assertEquals("2nd-value", actual.get());
    }

    @Test
    void noArguments() {
        final var mapping = new WikiNameMapping();
        final var any = mapping.getNewName(null);

        assertTrue(mapping.isEmpty());
        assertFalse(any.isPresent());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    void invalidArgumentLength(final int argsLength) {
        final var actual = assertThrows(IllegalArgumentException.class, () -> {
            final var args = IntStream.range(0, argsLength).mapToObj(Integer::toString).toArray(String[]::new);
            new WikiNameMapping(args);
        });

        assertEquals("Invalid argument length: " + argsLength, actual.getMessage());
    }

}