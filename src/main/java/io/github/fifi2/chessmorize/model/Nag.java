package io.github.fifi2.chessmorize.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * NAG stands for Numeric Annotation Glyphs
 */

@RequiredArgsConstructor
@Getter
@Schema(description = "Numeric Annotation Glyph (NAG) for chess moves, e.g. GOOD_MOVE, MISTAKE, etc.")
public enum Nag {

    @Schema(description = "A good move")
    GOOD_MOVE("!"),

    @Schema(description = "A mistake")
    MISTAKE("?"),

    @Schema(description = "A brilliant move")
    BRILLANT_MOVE("!!"),

    @Schema(description = "A blunder")
    BLUNDER("??"),

    @Schema(description = "A speculative move")
    SPECULATIVE_MOVE("!?"),

    @Schema(description = "A dubious move")
    DUBIOUS_MOVE("?!");

    private final String glyph;

}
