package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.api.LichessApiClient;
import io.github.fifi2.chessmorize.config.properties.TrainingProperties;
import io.github.fifi2.chessmorize.converter.PgnGamesToBookConverter;
import io.github.fifi2.chessmorize.error.exception.pgn.PgnException;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.model.LineMove;
import io.github.fifi2.chessmorize.model.Move;
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
     * @return a Mono of Book.
     */
    public Mono<Book> createBook(final String studyId) {

        return this.lichessApiClient.getStudyPGN(studyId)
            .map(pgn -> parsePgn(pgn, studyId))
            .map(pgnGames -> this.pgnGamesToBookConverter.convert(
                pgnGames,
                studyId))
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
        final long duration = Duration.between(start, Instant.now()).toMillis();
        log.info("Parse PGN {} in {} ms", studyId, duration);
        return pgnGames;
    }

}
