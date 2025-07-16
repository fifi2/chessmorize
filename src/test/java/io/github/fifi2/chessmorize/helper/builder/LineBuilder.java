package io.github.fifi2.chessmorize.helper.builder;

import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.model.LineMove;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class LineBuilder {

    private final Line line;

    public LineBuilder id(final UUID id) {

        this.line.setId(id);
        return this;
    }

    public LineBuilder chapterId(final UUID chapterId) {

        this.line.setChapterId(chapterId);
        return this;
    }

    private LineBuilder withMove(final LineMove move) {

        this.line.getMoves().add(move);
        return this;
    }

    public LineBuilder withMove(final Consumer<LineMoveBuilder> builder) {

        final LineMove move = LineMove.builder()
            .moveId(UUID.randomUUID())
            .build();

        builder.accept(new LineMoveBuilder(move));
        return this.withMove(move);
    }

    public LineBuilder withMove(final String uci,
                                final String comment) {

        return this.withMove(m -> m
            .uci(uci)
            .comment(comment));
    }

    public LineBuilder withMove(final String uci) {

        return this.withMove(m -> m
            .uci(uci));
    }

    public LineBuilder boxId(final int boxId) {

        this.line.setBoxId(boxId);
        return this;
    }

    public LineBuilder lastTraining(final Instant lastTraining) {

        this.line.setLastTraining(lastTraining);
        return this;
    }

    public LineBuilder lastCalendarSlot(final Integer lastCalendarSlot) {

        this.line.setLastCalendarSlot(lastCalendarSlot);
        return this;
    }

}
