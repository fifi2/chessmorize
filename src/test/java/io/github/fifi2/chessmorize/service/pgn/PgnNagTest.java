package io.github.fifi2.chessmorize.service.pgn;

import io.github.fifi2.chessmorize.service.pgn.PgnNag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PgnNagTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        !  | GOOD_MOVE
        ?  | MISTAKE
        !! | BRILLANT_MOVE
        ?? | BLUNDER
        !? | SPECULATIVE_MOVE
        ?! | DUBIOUS_MOVE
        XX |
           |
        """)
    void fromGlyph(String glyph, PgnNag expectedNag) {

        assertThat(PgnNag.fromGlyph(glyph).orElse(null))
            .isEqualTo(expectedNag);
    }

}
