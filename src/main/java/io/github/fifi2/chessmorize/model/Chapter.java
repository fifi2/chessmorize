package io.github.fifi2.chessmorize.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Chapter {

    private final UUID id;
    private final String title;
    private final List<Move> nextMoves;

}
