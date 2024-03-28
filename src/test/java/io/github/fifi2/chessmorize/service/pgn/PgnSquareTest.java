package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.PgnSquare;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PgnSquareTest {

    @DisplayName("Instantiate a PgnSquare with a null square name:")
    @Test
    void constructor_withNullName() {

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> new PgnSquare(null));
    }

    @DisplayName("Instantiate a PgnSquare by its name:")
    @ParameterizedTest(name = "{index}: with name {2}")
    @MethodSource("getArguments")
    void constructor_withName(final int expectedFileIdx,
                              final int expectedRankIdx,
                              final String squareName) {

        final PgnSquare square = new PgnSquare(squareName);
        assertThat(square.getFileIdx()).isEqualTo(expectedFileIdx);
        assertThat(square.getRankIdx()).isEqualTo(expectedRankIdx);
    }

    @DisplayName("Get a square name:")
    @ParameterizedTest(name = "{index}: with file index {0} and rank index {1}")
    @MethodSource("getArguments")
    void getName(final int fileIdx,
                 final int rankIdx,
                 final String expectedSquareName) {

        assertThat(new PgnSquare(fileIdx, rankIdx).getName())
            .isEqualTo(expectedSquareName);
    }

    @DisplayName("Is matching a disambiguating move information:")
    @ParameterizedTest(name = "{index}: when disambiguating move is {0}" +
        " and square {1}")
    @CsvSource(delimiter = '|', textBlock = """
           | b8 | true
        b  | b8 | true
        b8 | b8 | true
        b6 | b8 | false
        f  | b8 | false
        f6 | b8 | false
        """)
    void isMatchingDisambiguatingMove(final String disambiguatingMove,
                                      final String squareName,
                                      final boolean expectedMatching) {

        final PgnSquare square = new PgnSquare(squareName);
        assertThat(square.isMatchingDisambiguatingMove(disambiguatingMove))
            .isEqualTo(expectedMatching);
    }

    @DisplayName("Get square validity:")
    @ParameterizedTest(name = "{index}: on file index {0} and rank index {1}")
    @CsvSource(delimiter = '|', textBlock = """
        0  | 0  | true
        1  | 1  | true
        1  | 2  | true
        2  | 1  | true
        6  | 7  | true
        7  | 7  | true
        -1 | 0  | false
        0  | -1 | false
        8  | 7  | false
        7  | 8  | false
        8  | 8  | false
        """)
    void isValid(final int fileIdx,
                 final int rankIdx,
                 final boolean expectedValidity) {

        final PgnSquare square = new PgnSquare(fileIdx, rankIdx);
        assertThat(square.isValid()).isEqualTo(expectedValidity);
    }

    @DisplayName("Get the distance between to squares:")
    @ParameterizedTest(name = "{index}: from {0} to {1}")
    @CsvSource(delimiter = '|', textBlock = """
        a1 | h8 | 7
        b1 | b8 | 7
        c1 | d2 | 1
        e4 | e5 | 1
        e4 | d4 | 1
        e4 | e3 | 1
        e4 | f4 | 1
        g1 | f3 | 2
        """)
    void getDistance(final String nameA,
                     final String nameB,
                     final int expectedDistance) {

        assertThat(new PgnSquare(nameA).getDistance(new PgnSquare(nameB)))
            .isEqualTo(expectedDistance);
    }

    private static Stream<Arguments> getArguments() {

        return Stream.of(
            Arguments.of(0, 7, "a1"), // bottom left
            Arguments.of(0, 0, "a8"), // bottom right
            Arguments.of(7, 7, "h1"), // top left
            Arguments.of(7, 0, "h8"), // top right
            Arguments.of(6, 0, "g8"), // near top file, top rank
            Arguments.of(7, 1, "h7"), // near top rank, top file
            Arguments.of(1, 7, "b1"), // near bottom file, bottom rank
            Arguments.of(0, 6, "a2"), // near bottom rank, bottom file
            Arguments.of(4, 4, "e4")  // middle
        );
    }

}
