package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.api.LichessApiClient;
import io.github.fifi2.chessmorize.config.properties.TrainingProperties;
import io.github.fifi2.chessmorize.converter.PgnGamesToBookConverter;
import io.github.fifi2.chessmorize.error.exception.ChapterNotFoundException;
import io.github.fifi2.chessmorize.error.exception.pgn.PgnException;
import io.github.fifi2.chessmorize.model.*;
import io.github.fifi2.chessmorize.repository.BookRepository;
import io.github.fifi2.chessmorize.service.pgn.PgnGame;
import io.github.fifi2.chessmorize.service.pgn.PgnParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final LichessApiClient lichessApiClient;
    private final PgnGamesToBookConverter pgnGamesToBookConverter;
    private final BookRepository bookRepository;
    private final TrainingProperties trainingProperties;

    /**
     * Create a book from a Lichess study id. The method will retrieve the PGN
     * study, parse it and convert the result as a Book. The resulting lines
     * will be generated and set to the book before it is saved and returned in
     * a Mono.
     *
     * @param studyId is the study id.
     * @param color is the player color (WHITE or BLACK).
     * @return a Mono of Book.
     */
    public Mono<Book> createBook(final String studyId,
                                 final Color color) {

        return this.lichessApiClient.getStudyPGN(studyId)
            .map(pgn -> parsePgn(pgn, studyId))
            .map(pgnGames -> this.pgnGamesToBookConverter.convert(
                pgnGames,
                studyId,
                color))
            .doOnNext(book -> book.setLines(this.createLines(book)))
            .flatMap(this.bookRepository::save);
    }

    /**
     * Retrieve a Book by its id and return it as a Mono.
     *
     * @param bookId is the id of the Book.
     * @return A Mono of the Book.
     */
    public Mono<Book> getOneBook(final UUID bookId) {

        return this.bookRepository.findById(bookId);
    }

    /**
     * Get all books.
     *
     * @return A Flux of all Books.
     */
    public Flux<Book> getAllBooks() {

        return this.bookRepository.findAll();
    }

    /**
     * Delete a Book by its id.
     *
     * @param bookId is the id of the Book.
     * @return A Mono containing the id of the deleted Book.
     */
    public Mono<UUID> deleteOneBook(final UUID bookId) {

        return this.bookRepository.deleteById(bookId);
    }

    /**
     * Flatten all moves from the book as a List of Lines.
     *
     * @param book is the Book to flatten.
     * @return A List of Line.
     */
    List<Line> createLines(final Book book) {

        List<Line> lines = book.getChapters()
            .stream()
            .flatMap(chapter -> chapter.getNextMoves()
                .stream()
                .flatMap(this::buildChapterLines)
                .map(moveIds -> Line.builder()
                    .id(UUID.randomUUID())
                    .chapterId(chapter.getId())
                    .moves(moveIds)
                    .build()))
            .collect(Collectors.toList());

        if (this.trainingProperties.isShuffled())
            Collections.shuffle(lines);

        return lines;
    }

    /**
     * Recursively flatten all possible paths from a Move and its next Moves.
     *
     * @param move is the Move to flatten.
     * @return A Stream of List of LineMove.
     */
    Stream<List<LineMove>> buildChapterLines(final Move move) {

        final List<Move> nextMoves = Optional
            .ofNullable(move.getNextMoves())
            .orElse(List.of());

        final LineMove currentLineMove = LineMove.builder()
            .moveId(move.getId())
            .uci(move.getUci())
            .comment(move.getComment())
            .build();

        if (nextMoves.isEmpty()) {
            return Stream.of(List.of(currentLineMove));
        }

        return nextMoves
            .stream()
            .flatMap(nextMove -> this.buildChapterLines(nextMove)
                .map(followingPath -> Stream.concat(
                        Stream.of(currentLineMove),
                        followingPath.stream())
                    .toList()));
    }

    /**
     * Enable or disable a chapter.
     *
     * @param bookId    The Book id.
     * @param chapterId The id of the Chapter to disable.
     * @param enabled   A boolean (true to enable, false to disable).
     * @return A Mono of the updated Book.
     */
    public Mono<Book> toggleChapter(final UUID bookId,
                                    final UUID chapterId,
                                    final boolean enabled) {

        return this.bookRepository.findById(bookId)
            .doOnNext(book -> {
                book.getChapters()
                    .stream()
                    .filter(chapter -> chapterId.equals(chapter.getId()))
                    .findAny()
                    .ifPresentOrElse(
                        chapter -> chapter.setEnabled(enabled),
                        () -> {
                            throw new ChapterNotFoundException(bookId, chapterId);
                        }
                    );
                if (!enabled) {
                    // Purge lines of disabled chapter
                    book.setLines(Optional
                        .ofNullable(book.getLines())
                        .orElse(List.of())
                        .stream()
                        .filter(line -> !chapterId.equals(line.getChapterId()))
                        .toList());
                } else {
                    // Refresh lines if chapter is enabled
                    // TODO Test and Integration test
                    this.refreshLines(book);
                }
            })
            .flatMap(this.bookRepository::update);
    }

    /**
     * Refresh the lines of a Book.
     *
     * @param book is the Book to refresh lines for.
     *             This method should be called when a chapter is enabled
     *             to regenerate its lines.
     */
    void refreshLines(final Book book) {

        // TODO + Test and Integration test
        return;
    }

    /**
     * Do the PGN parsing of the Book from its study.
     *
     * @param pgn     is the study as a String (the PGN).
     * @param studyId is the study id on Lichess.
     * @return A List of PgnGame.
     */
    private static List<PgnGame> parsePgn(final String pgn,
                                          final String studyId) {

        // pgn parsing
        List<PgnGame> pgnGames;
        final Instant start = Instant.now();
        try {
            pgnGames = PgnParser.parse(pgn);
        } catch (Exception e) {
            throw new PgnException(studyId, e);
        }
        log.info(
            "Parse PGN {} in {} ms",
            studyId,
            Duration.between(start, Instant.now()).toMillis());
        return pgnGames;
    }

}
