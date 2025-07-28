package io.github.fifi2.chessmorize.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A move in a chess chapter")
public class Move {

    @Schema(
        description = "The move id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private UUID id;

    @Schema(
        description = "The move SAN (Standard Algebraic Notation)",
        example = "e4")
    private String san;

    @Schema(
        description = "The move UCI (Universal Chess Interface) notation",
        example = "e2e4")
    private String uci;

    @Schema(
        description = "The move NAG (Numeric Annotation Glyph)",
        example = "GOOD_MOVE")
    private Nag nag;

    @Schema(
        description = "An optional move comment",
        example = "It's a good opening move")
    private String comment;

    @Schema(
        description = "The Color of the player making the move",
        example = "WHITE")
    private Color color;

    @Schema(description = "The next moves, as a list of Move")
    private List<Move> nextMoves;

}
