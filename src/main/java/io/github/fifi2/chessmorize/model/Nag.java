package io.github.fifi2.chessmorize.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * NAG stands for Numeric Annotation Glyphs
 */

@RequiredArgsConstructor
@Getter
public enum Nag {

    GOOD_MOVE("!"),
    MISTAKE("?"),
    BRILLANT_MOVE("!!"),
    BLUNDER("??"),
    SPECULATIVE_MOVE("!?"),
    DUBIOUS_MOVE("?!");

    private final String glyph;

}
