package io.github.fifi2.chessmorize.helper.builder;

import io.github.fifi2.chessmorize.model.Chapter;
import io.github.fifi2.chessmorize.model.Move;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ChapterBuilder {

    private final Chapter chapter;

    public ChapterBuilder id(final UUID id) {

        this.chapter.setId(id);
        return this;
    }

    public ChapterBuilder title(final String title) {

        this.chapter.setTitle(title);
        return this;
    }

    public ChapterBuilder enabled(final boolean enabled) {

        this.chapter.setEnabled(enabled);
        return this;
    }

    private ChapterBuilder withNextMove(final Move move) {

        this.chapter.getNextMoves().add(move);
        return this;
    }

    public ChapterBuilder withNextMove(final Consumer<MoveBuilder> builder) {

        final Move move = Move.builder()
            .id(UUID.randomUUID())
            .nextMoves(new ArrayList<>())
            .build();

        builder.accept(new MoveBuilder(move));
        return this.withNextMove(move);
    }

}
