package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.Color;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ColorTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        w | WHITE
        b | BLACK
        """)
    void fromFenNotation(final Character fenNotation, final Color expectedColor) {

        assertThat(Color.fromFenNotation(fenNotation)).isEqualTo(expectedColor);
        assertThat(Color.fromFenNotation(String.valueOf(fenNotation)))
            .isEqualTo(expectedColor);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
          | java.lang.IllegalArgumentException
        x | java.util.NoSuchElementException
        """)
    void fromFenNotation_withUnknownLetter(
        final Character fenNotation,
        final Class<? extends Exception> expectedException) {

        assertThatExceptionOfType(expectedException)
            .isThrownBy(() -> Color.fromFenNotation(fenNotation));
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLACK"})
    void fromName(String color) {

        assertThat(Color.fromName(color))
            .isNotNull()
            .isInstanceOf(Color.class)
            .extracting(Enum::name)
            .isEqualTo(color);
    }

}
