package io.github.fifi2.chessmorize.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Move {

    private final UUID id;
    private final String san;
    private final String uci;
    private final Nag nag;
    private final List<Move> nextMoves;

    @JsonIgnore
    public Move getNext() {

        return this.nextMoves.getFirst();
    }

}
