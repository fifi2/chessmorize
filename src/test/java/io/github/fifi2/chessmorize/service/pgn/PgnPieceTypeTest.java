package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.PgnPieceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PgnPieceTypeTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        P | PAWN
        R | ROOK
        N | KNIGHT
        B | BISHOP
        Q | QUEEN
        K | KING
        """)
    void fromLetter(final Character letter,
                    final PgnPieceType expectedPieceType) {

        assertThat(PgnPieceType.fromLetter(letter))
            .isEqualTo(expectedPieceType);
        assertThat(PgnPieceType.fromLetter(String.valueOf(letter)))
            .isEqualTo(expectedPieceType);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
          | java.lang.IllegalArgumentException
        X | java.util.NoSuchElementException
        """)
    void fromLetter_withUnknownLetter(
        final Character letter,
        final Class<? extends Exception> expectedException) {

        assertThatExceptionOfType(expectedException)
            .isThrownBy(() -> PgnPieceType.fromLetter(letter));
    }

}
