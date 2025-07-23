package io.github.fifi2.chessmorize.it;

import io.github.fifi2.chessmorize.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.controller.api.dto.ToggleChapterRequest;
import io.github.fifi2.chessmorize.controller.api.dto.TrainingResultRequest;
import io.github.fifi2.chessmorize.helper.builder.BookBuilder;
import io.github.fifi2.chessmorize.helper.builder.LineBuilder;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.helper.Constants.Api;
import static io.github.fifi2.chessmorize.helper.Constants.Json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TrainingApiTest extends AbstractSpringBootTest {

    private static final BiFunction<UUID, UUID, Consumer<LineBuilder>> INTRO =
        (lineId, chapterId) -> line -> line
            .id(lineId)
            .chapterId(chapterId)
            .withMove("e2e4")
            .withMove("c7c6", "The Caro-Kann!");

    private static final List<String> INTRO_UCIS = List.of("e2e4", "c7c6");

    private static final BiFunction<UUID, UUID, Consumer<LineBuilder>> SHORT1 =
        (lineId, chapterId) -> line -> line
            .id(lineId)
            .chapterId(chapterId)
            .withMove("e2e4")
            .withMove("c7c6")
            .withMove("d2d4")
            .withMove("d7d5")
            .withMove("e4e5")
            .withMove("c8f5")
            .withMove("g1f3", "Short 1")
            .withMove("e7e6")
            .withMove("f1e2")
            .withMove("c6c5");

    private static final List<String> SHORT1_UCIS = List.of(
        "e2e4", "c7c6",
        "d2d4", "d7d5",
        "e4e5", "c8f5",
        "g1f3", "e7e6",
        "f1e2", "c6c5");

    private static final BiFunction<UUID, UUID, Consumer<LineBuilder>> SHORT2 =
        (lineId, chapterId) -> line -> line
            .id(lineId)
            .chapterId(chapterId)
            .withMove("e2e4")
            .withMove("c7c6")
            .withMove("d2d4")
            .withMove("d7d5")
            .withMove("e4e5")
            .withMove("c8f5")
            .withMove("f1e2", "Short 2")
            .withMove("e7e6")
            .withMove("g1f3");

    private static final List<String> SHORT2_UCIS = List.of(
        "e2e4", "c7c6",
        "d2d4", "d7d5",
        "e4e5", "c8f5",
        "f1e2", "e7e6",
        "g1f3");

    private static final BiFunction<UUID, UUID, Consumer<LineBuilder>> KARPOV =
        (lineId, chapterId) -> line -> line
            .id(lineId)
            .chapterId(chapterId)
            .withMove("e2e4")
            .withMove("c7c6")
            .withMove("d2d4")
            .withMove("d7d5")
            .withMove("b1c3")
            .withMove("d5e4")
            .withMove("c3e4")
            .withMove("b8d7", "Karpov");

    @Autowired
    private WebTestClient webTestClient;

    private static Stream<Arguments> getNextLine_noContent() {

        final UUID chapterId = UUID.randomUUID();

        final Consumer<LineBuilder> lineBuilder = INTRO.apply(
            UUID.randomUUID(),
            chapterId);

        return Stream.of(
            // No lines
            Arguments.of(BookBuilder.builder()
                .id(UUID.randomUUID())
                .color(Color.BLACK)
                .build()),
            // No lines to train
            Arguments.of(BookBuilder.builder()
                .color(Color.WHITE)
                .calendarSlot(2) // boxes 0, 2
                .withChapter(chapterId, "Chapter title")
                .withLine(lineBuilder.andThen(line -> line.boxId(1)))
                .build()),
            // No lines to train, excluding those that goes back in the 0 box
            // during the session.
            Arguments.of(BookBuilder.builder()
                .calendarSlot(2) // boxes 0, 2
                .color(Color.WHITE)
                .withChapter(chapterId, "Chapter title")
                .withLine(lineBuilder.andThen(line -> line
                    .boxId(0)
                    .lastTraining(Instant.now())
                    .lastCalendarSlot(2)))
                .build()));
    }

    @ParameterizedTest
    @MethodSource
    void getNextLine_noContent(final Book book) {

        this.saveBook(book);

        this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, book.getId())
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void getNextLine_nominal() {

        final UUID bookId = UUID.randomUUID();
        final UUID introChapterId = UUID.randomUUID();
        final UUID introLineId = UUID.randomUUID();
        final UUID shortChapterId = UUID.randomUUID();
        final UUID karpovChapterId = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .color(Color.BLACK)
            .withChapter(introChapterId, "Caro-Kann, Introduction")
            .withChapter(shortChapterId, "Caro-Kann, Short variation")
            .withChapter(karpovChapterId, "Caro-Kann, Karpov")
            .withLine(INTRO.apply(introLineId, introChapterId))
            .withLine(SHORT1.apply(UUID.randomUUID(), shortChapterId))
            .withLine(SHORT2.apply(UUID.randomUUID(), shortChapterId))
            .withLine(KARPOV.apply(UUID.randomUUID(), karpovChapterId))
            .build());

        this.runAndAssertNextLine(
            bookId,
            introLineId, introChapterId,
            INTRO_UCIS,
            0, null,
            null);
    }

    @Test
    void getNextLine_withDisabledChapter() {

        final UUID bookId = UUID.randomUUID();
        final UUID introChapterId = UUID.randomUUID();
        final UUID introLineId = UUID.randomUUID();
        final UUID shortChapterId = UUID.randomUUID();
        final UUID shortLine1Id = UUID.randomUUID();
        final UUID karpovChapterId = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .color(Color.BLACK)
            .withChapter(introChapterId, "Caro-Kann, Introduction")
            .withChapter(shortChapterId, "Caro-Kann, Short variation")
            .withChapter(karpovChapterId, "Caro-Kann, Karpov")
            .withLine(INTRO.apply(introLineId, introChapterId))
            .withLine(SHORT1.apply(shortLine1Id, shortChapterId))
            .withLine(SHORT2.apply(UUID.randomUUID(), shortChapterId))
            .withLine(KARPOV.apply(UUID.randomUUID(), karpovChapterId))
            .build());

        // toggle chapter introduction
        // NB: Disabling a chapter deletes all its lines so the test would not
        // make sense if we did not use the toggle endpoint to disable the
        // chapter.
        this.webTestClient.put()
            .uri(Api.TOGGLE_CHAPTER)
            .bodyValue(ToggleChapterRequest.builder()
                .bookId(bookId)
                .chapterId(introChapterId)
                .enabled(false)
                .build())
            .exchange()
            .expectStatus().isNoContent();

        this.runAndAssertNextLine(
            bookId,
            shortLine1Id, shortChapterId,
            SHORT1_UCIS,
            0, null,
            null);
    }

    @Test
    void getNextLine_complexCase() {

        final UUID bookId = UUID.randomUUID();
        final UUID introChapterId = UUID.randomUUID();
        final UUID introLineId = UUID.randomUUID();
        final UUID shortChapterId = UUID.randomUUID();
        final UUID shortLine1Id = UUID.randomUUID();
        final UUID shortLine2Id = UUID.randomUUID();

        final Instant yesterday = Instant.now()
            .minus(1, ChronoUnit.DAYS);
        final Instant fiveMinutesAgo = Instant.now()
            .minus(5, ChronoUnit.MINUTES);

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .color(Color.BLACK)
            .calendarSlot(2) // boxes 0, 2
            .withChapter(introChapterId, "Caro-Kann, Introduction")
            .withChapter(shortChapterId, "Caro-Kann, Short variation")
            // A line successfully trained a moment ago
            .withLine(INTRO.apply(introLineId, introChapterId)
                .andThen(line -> line
                    .boxId(3)
                    .lastTraining(fiveMinutesAgo)
                    .lastCalendarSlot(2)))
            // A line failed a moment ago
            .withLine(SHORT1.apply(shortLine1Id, shortChapterId)
                .andThen(line -> line
                    .boxId(0)
                    .lastTraining(fiveMinutesAgo)
                    .lastCalendarSlot(2)))
            // A line that goes back to box 0 yesterday
            .withLine(SHORT2.apply(shortLine2Id, shortChapterId)
                .andThen(line -> line
                    .boxId(0)
                    .lastTraining(yesterday)
                    .lastCalendarSlot(1)))
            .build());

        this.runAndAssertNextLine(
            bookId,
            shortLine2Id, shortChapterId,
            SHORT2_UCIS,
            0, 1,
            yesterday);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        0 | true  | 1
        0 | false | 0
        1 | true  | 2
        1 | false | 0
        5 | true  | 5
        """)
    void setResult(final int initialBoxId,
                   final boolean success,
                   final int expectedBoxId) {

        final UUID bookId = UUID.randomUUID();
        final UUID introChapterId = UUID.randomUUID();
        final UUID introLineId = UUID.randomUUID();
        final UUID shortChapterId = UUID.randomUUID();
        final UUID shortLine1Id = UUID.randomUUID();
        final UUID karpovChapterId = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .color(Color.BLACK)
            .calendarSlot(1) // boxes 0, 1
            .withChapter(introChapterId, "Caro-Kann, Introduction")
            .withChapter(shortChapterId, "Caro-Kann, Short variation")
            .withChapter(karpovChapterId, "Caro-Kann, Karpov")
            .withLine(INTRO.apply(introLineId, introChapterId)
                .andThen(line -> line.boxId(initialBoxId)))
            .withLine(SHORT1.apply(shortLine1Id, shortChapterId))
            .withLine(SHORT2.apply(UUID.randomUUID(), shortChapterId))
            .withLine(KARPOV.apply(UUID.randomUUID(), karpovChapterId))
            .build());

        this.runAndAssertSetResult(
            bookId,
            introLineId, success, introChapterId,
            expectedBoxId, 1);

        this.runAndAssertNextLine(
            bookId,
            shortLine1Id, shortChapterId,
            SHORT1_UCIS,
            0, null,
            null);
    }

    private void runAndAssertNextLine(final UUID bookId,
                                      final UUID expectedLineId,
                                      final UUID expectedChapterId,
                                      final List<String> expectedUcis,
                                      final int expectedBoxId,
                                      final Integer expectedLastSlot,
                                      final Instant expectedLastTraining) {

        WebTestClient.BodyContentSpec spec = this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, bookId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(Json.NEXT_LINE_ID).isEqualTo(expectedLineId.toString())
            .jsonPath(Json.NEXT_LINE_CHAPTER_ID).isEqualTo(expectedChapterId.toString())
            .jsonPath(Json.NEXT_LINE_MOVES_SIZE).isEqualTo(expectedUcis.size())
            .jsonPath(Json.NEXT_LINE_BOX_ID).isEqualTo(expectedBoxId)
            .jsonPath(Json.NEXT_LINE_LAST_TRAINING).value(
                lastTraining -> Optional
                    .ofNullable(expectedLastTraining)
                    .ifPresentOrElse(
                        expected -> assertThat(Instant.parse(lastTraining.toString()))
                            .isEqualTo(expected),
                        () -> assertThat(lastTraining).isNull()))
            .jsonPath(Json.NEXT_LINE_LAST_CALENDAR_SLOT).isEqualTo(expectedLastSlot);

        for (int i = 0; i < expectedUcis.size(); i++) {
            spec = spec
                .jsonPath(Json.NEXT_LINE_MOVE_UCI, i)
                .isEqualTo(expectedUcis.get(i));
        }
    }

    private void runAndAssertSetResult(
        final UUID bookId,
        final UUID lineId,
        final boolean result,
        final UUID expectedChapterId,
        final int expectedBoxId,
        final int expectedLastSlot) {

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT)
            .bodyValue(TrainingResultRequest.builder()
                .bookId(bookId)
                .lineId(lineId)
                .result(result)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(Json.NEXT_LINE_ID).isEqualTo(lineId.toString())
            .jsonPath(Json.NEXT_LINE_CHAPTER_ID).isEqualTo(expectedChapterId.toString())
            .jsonPath(Json.NEXT_LINE_BOX_ID).isEqualTo(expectedBoxId)
            .jsonPath(Json.NEXT_LINE_LAST_TRAINING).value(
                d -> assertThat(Instant.parse(d.toString()))
                    .isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS)))
            .jsonPath(Json.NEXT_LINE_LAST_CALENDAR_SLOT).isEqualTo(expectedLastSlot);
    }

}
