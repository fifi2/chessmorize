package io.github.fifi2.chessmorize.helper.builder;

import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Chapter;
import io.github.fifi2.chessmorize.model.Color;
import io.github.fifi2.chessmorize.model.Line;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class BookBuilder {

    private Book book;

    public static BookBuilder builder() {

        final BookBuilder builder = new BookBuilder();
        builder.book = Book.builder()
            .id(UUID.randomUUID())
            .studyId(UUID.randomUUID().toString().substring(0, 8))
            .name("Test Book")
            .chapters(new ArrayList<>())
            .lines(new ArrayList<>())
            .build();
        return builder;
    }

    public Book build() {

        return this.book;
    }

    public BookBuilder id(final UUID id) {

        this.book.setId(id);
        return this;
    }

    public BookBuilder name(final String name) {

        this.book.setName(name);
        return this;
    }

    public BookBuilder color(final Color color) {

        this.book.setColor(color);
        return this;
    }

    public BookBuilder studyId(final String studyId) {

        this.book.setStudyId(studyId);
        return this;
    }

    public BookBuilder withChapter(final Consumer<ChapterBuilder> builder) {

        final Chapter chapter = Chapter.builder()
            .id(UUID.randomUUID())
            .enabled(true)
            .nextMoves(new ArrayList<>())
            .build();

        builder.accept(new ChapterBuilder(chapter));
        this.book.getChapters().add(chapter);
        return this;
    }

    public BookBuilder withChapter(final UUID id,
                                   final String title) {

        return this.withChapter(chapter -> chapter
            .id(id)
            .title(title));
    }

    public BookBuilder withLine(final Consumer<LineBuilder> builder) {

        final Line line = Line.builder()
            .id(UUID.randomUUID())
            .chapterId(UUID.randomUUID())
            .moves(new ArrayList<>())
            .boxId(0)
            .build();

        builder.accept(new LineBuilder(line));
        this.book.getLines().add(line);
        return this;
    }

    public BookBuilder calendarSlot(final int slot) {

        this.book.setCalendarSlot(slot);
        return this;
    }

}
