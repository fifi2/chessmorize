package io.github.fifi2.chessmorize.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Move {

    private UUID id;
    private String san;
    private String uci;
    private Nag nag;
    private String comment;
    private final List<Move> nextMoves;

}
