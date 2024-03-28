package io.github.fifi2.chessmorize.service.pgn;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum PgnNag {

    GOOD_MOVE("!"),
    MISTAKE("?"),
    BRILLANT_MOVE("!!"),
    BLUNDER("??"),
    SPECULATIVE_MOVE("!?"),
    DUBIOUS_MOVE("?!");

    private final String glyph;

    /**
     * Get a PgnNag from a glyph
     *
     * @param glyph the glyph to search for
     * @return An optional of PgnNag
     */
    static Optional<PgnNag> fromGlyph(final String glyph) {

        return Arrays.stream(PgnNag.values())
            .filter(nag -> nag.glyph.equals(glyph))
            .findFirst();
    }

}
