package org.mtzky.backlog.wiki;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikiParserTest {

    @ParameterizedTest
    @CsvSource({
            "foo [[bar]] bar [[bar/baz]] qux, foo [[BAR]] bar [[bar/baz]] qux"
    })
    void linksMigrated(final String input, final String expected) {
        final var mapping = new WikiNameMapping().args("bar", "BAR");
        final var sut = new WikiParser(input);

        final var actual = sut.migrateLinks(mapping);

        assertEquals(expected, actual);
    }

    @Test
    void linksListed() {
        final var sut = new WikiParser("foo [[bar]] bar [[bar/baz]] qux");

        final var actual = sut.streamLinks();

        assertEquals(List.of("bar", "bar/baz"), actual.toList());
    }

}