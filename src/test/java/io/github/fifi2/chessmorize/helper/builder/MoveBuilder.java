package io.github.fifi2.chessmorize.helper.builder;

import io.github.fifi2.chessmorize.model.Color;
import io.github.fifi2.chessmorize.model.Move;
import io.github.fifi2.chessmorize.model.Nag;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class MoveBuilder {

    private final Move move;

    public MoveBuilder id(final UUID id) {

        this.move.setId(id);
        return this;
    }

    public MoveBuilder san(final String san) {

        this.move.setSan(san);
        return this;
    }

    public MoveBuilder uci(final String uci) {

        this.move.setUci(uci);
        return this;
    }

    public MoveBuilder nag(final Nag nag) {

        this.move.setNag(nag);
        return this;
    }

    public MoveBuilder comment(final String comment) {

        this.move.setComment(comment);
        return this;
    }

    public MoveBuilder color(final Color color) {

        this.move.setColor(color);
        return this;
    }

    private MoveBuilder withNextMove(final Move move) {

        this.move.getNextMoves().add(move);
        return this;
    }

    public MoveBuilder withNextMove(final Consumer<MoveBuilder> builder) {

        final Move nextMove = Move.builder()
            .id(UUID.randomUUID())
            .nextMoves(new ArrayList<>())
            .build();

        builder.accept(new MoveBuilder(nextMove));
        return this.withNextMove(nextMove);
    }

}
