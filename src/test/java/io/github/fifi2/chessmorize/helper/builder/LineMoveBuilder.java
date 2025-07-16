package io.github.fifi2.chessmorize.helper.builder;

import io.github.fifi2.chessmorize.model.LineMove;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class LineMoveBuilder {

    private final LineMove move;

    public LineMoveBuilder moveId(final UUID id) {
        this.move.setMoveId(id);
        return this;
    }

    public LineMoveBuilder uci(final String uci) {
        this.move.setUci(uci);
        return this;
    }

    public LineMoveBuilder comment(final String comment) {
        this.move.setComment(comment);
        return this;
    }

}
