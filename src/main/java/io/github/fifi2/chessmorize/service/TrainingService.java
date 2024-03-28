package io.github.fifi2.chessmorize.service;

import io.github.fifi2.chessmorize.config.properties.BoxProperties;
import io.github.fifi2.chessmorize.error.exception.LineNotFoundException;
import io.github.fifi2.chessmorize.error.exception.NoTrainingLineException;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final BookRepository bookRepository;
    private final BoxProperties boxProperties;


    /**
     * Given a Book id, retrieve the Book and pick the next line to train.
     *
     * @param bookId is the Book id.
     * @return a Mono of Line (empty if no line to train is found).
     */
    public Mono<Line> getNextLine(final UUID bookId) {

        return this.bookRepository.findById(bookId)
            .map(this::pickNextLine)
            .flatMap(optionalLine -> optionalLine
                .map(Mono::just)
                .orElseThrow(() -> new NoTrainingLineException(bookId)));
    }

    /**
     * Return the next line to review during the training session. Resulting
     * Lines are sorted by boxId (ASC) and lastTraining (ASC) and an Optional is
     * returned with the first Line (empty if no Line is found).
     *
     * @param book The current studied Book.
     * @return An optional of Line. Empty if the training session is over.
     */
    Optional<Line> pickNextLine(final Book book) {

        final int currentCalendarSlot = book.getCalendarSlot();
        final List<Integer> currentBoxes = this.boxProperties.getCalendar()
            .get(currentCalendarSlot);

        return book.getLines()
            .stream()
            // filter lines related to the session boxes
            .filter(line -> currentBoxes.contains(line.getBoxId()))
            // filter lines that get back in box 0 during this training session
            .filter(line -> Optional
                .ofNullable(line.getLastCalendarSlot())
                .map(lastSlot -> !lastSlot.equals(currentCalendarSlot)
                    || line.hasNotBeenTrainedToday())
                .orElse(true))
            .sorted(Comparator
                .comparing(Line::getBoxId)
                .thenComparing((a, b) -> {
                    final Instant lastTrainingA = a.getLastTraining();
                    final Instant lastTrainingB = b.getLastTraining();
                    if (lastTrainingA == null && lastTrainingB == null)
                        return 0;
                    if (lastTrainingA == null)
                        return -1;
                    if (lastTrainingB == null)
                        return 1;
                    return lastTrainingA.compareTo(lastTrainingB);
                }))
            .findAny();
    }

    /**
     * Save the training result on the Line. The Line box will be updated to the
     * first one if {@code result} is false, to the next one otherwise. The
     * metadata of the Line will be updated accordingly ({@code lastTraining}
     * and {@code lastCalendarSlot}).
     *
     * @param bookId is the Book id.
     * @param lineId is the Line id.
     * @param result is the training result.
     * @return a Mono of the updated Line.
     */
    public Mono<Line> setLineResult(final UUID bookId,
                                    final UUID lineId,
                                    final boolean result) {

        return this.bookRepository.findById(bookId)
            .flatMap(book -> {
                final Optional<Line> optionalLine = book.getLines()
                    .stream()
                    .filter(line -> lineId.equals(line.getId()))
                    .findAny();
                if (optionalLine.isPresent()) {
                    final Line line = optionalLine.get();
                    line.setBoxId(result ? this.computeNextBoxId(line) : 0);
                    line.setLastTraining(Instant.now());
                    line.setLastCalendarSlot(book.getCalendarSlot());
                    return this.bookRepository.update(book)
                        .thenReturn(line);
                }
                throw new LineNotFoundException(bookId, lineId);
            });
    }

    /**
     * Force next training session.
     *
     * @param bookId is the Book id.
     * @return a Mono of the updated Book.
     */
    public Mono<Book> nextCalendarSlot(final UUID bookId) {

        return this.bookRepository.findById(bookId)
            .doOnNext(book -> {
                final int nextSlot = book.getCalendarSlot() + 1;
                book.setCalendarSlot(
                    nextSlot >= this.boxProperties.getCalendar().size()
                        ? 0
                        : nextSlot);
            })
            .flatMap(this.bookRepository::update);
    }

    /**
     * Compute the new box id for a given line. It normally goes to the next box
     * unless it already is in the last configured one.
     *
     * @param line is the Line to update.
     * @return the new box id.
     */
    int computeNextBoxId(final Line line) {

        final int nextBoxId = line.getBoxId() + 1;
        return this.boxProperties.getMaxNumber().compareTo(nextBoxId) >= 0
            ? nextBoxId
            : line.getBoxId();
    }

}
