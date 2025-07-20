package io.github.fifi2.chessmorize.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@Schema(description = "A chapter in a book, containing a list of moves")
public class Chapter {

    @Schema(
        description = "The chapter id",
        example = "d3c4b5a6-7e8f-4a2b-9c1d-2e3f4a5b6c7d")
    private UUID id;

    @Schema(
        description = "The chapter title",
        example = "The exchange variation")
    private String title;

    @Schema(
        description = "The enabled state of the chapter",
        example = "true")
    private boolean enabled;

    @Schema(description = "The next moves, as a list of Move")
    private final List<Move> nextMoves;

}
