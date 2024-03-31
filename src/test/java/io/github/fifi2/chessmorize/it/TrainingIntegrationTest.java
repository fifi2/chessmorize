package io.github.fifi2.chessmorize.it;

import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.controller.api.dto.NextCalendarSlotRequest;
import io.github.fifi2.chessmorize.controller.api.dto.ToggleChapterRequest;
import io.github.fifi2.chessmorize.controller.api.dto.TrainingResultRequest;
import io.github.fifi2.chessmorize.helper.AbstractLichessTest;
import io.github.fifi2.chessmorize.helper.ObjectWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.helper.Constants.Api;
import static io.github.fifi2.chessmorize.helper.Constants.Json;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "chessmorize.training.shuffled=false"
})
class TrainingIntegrationTest extends AbstractLichessTest {

    record TrainingInstants(Instant start, Instant end) {
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void trainingApi_nominal() {

        final String studyId = "study-id";
        final ObjectWrapper<UUID> bookId = new ObjectWrapper<>();
        final ObjectWrapper<String> introChapter = new ObjectWrapper<>();
        final ObjectWrapper<String> shortChapter = new ObjectWrapper<>();
        final ObjectWrapper<String> karpovChapter = new ObjectWrapper<>();
        final ObjectWrapper<String> shortLine1 = new ObjectWrapper<>();
        final ObjectWrapper<String> shortLine2 = new ObjectWrapper<>();
        final ObjectWrapper<String> karpovLine = new ObjectWrapper<>();
        final List<String> shortLine1Ucis = List.of(
            "e2e4", "c7c6",
            "d2d4", "d7d5",
            "e4e5", "c8f5",
            "g1f3", "e7e6",
            "f1e2", "c6c5");
        final List<String> shortLine2Ucis = List.of(
            "e2e4", "c7c6",
            "d2d4", "d7d5",
            "e4e5", "c8f5",
            "f1e2", "e7e6",
            "g1f3");
        final List<String> karpovLineUcis = List.of(
            "e2e4", "c7c6",
            "d2d4", "d7d5",
            "b1c3", "d5e4",
            "c3e4", "b8d7");
        final Map<String, TrainingInstants> lastTraining = new HashMap<>();

        this.lichessMockResponse("""
            [Event "TrainingIntegrationTest: Caro-Kann, Introduction"]
            
            1. e4 c6 { It's the Caro-Kann! } *
            
            
            [Event "TrainingIntegrationTest: Caro-Kann, Short variation"]
            
            1. e4 c6 2. d4 d5 3. e5 Bf5 4. Nf3 { Short 1 } (4. Be2 { Short 2 } e6 5. Nf3) 4... e6 5. Be2 c5 *
            
            
            [Event "TrainingIntegrationTest: Caro-Kann, Karpov"]
            
            1. e4 c6 2. d4 d5 3. Nc3 dxe4 4. Nxe4 Nd7 { Karpov } *
            """);

        // create a book
        final WebTestClient.BodyContentSpec postBody =
            this.webTestClient
                .post()
                .uri(Api.BOOKS)
                .bodyValue(BookCreationRequest.builder()
                    .studyId(studyId)
                    .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath(Json.ID).value(id ->
                    bookId.set(UUID.fromString(id.toString())));

        // get the book
        final WebTestClient.BodyContentSpec getBody =
            this.webTestClient
                .get()
                .uri(Api.BOOK, bookId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(Json.ID).isEqualTo(bookId.get().toString());

        // assert POST and GET responses
        Stream.of(postBody, getBody).forEach(body -> body
            .jsonPath(Json.STUDY_ID).isEqualTo(studyId)
            .jsonPath(Json.NAME).isEqualTo("TrainingIntegrationTest")
            .jsonPath(Json.CALENDAR_SLOT).isEqualTo(0)
            .jsonPath(Json.CHAPTERS_SIZE).isEqualTo(3)
            .jsonPath(Json.LINES_SIZE).isEqualTo(4)
            // chapter - Introduction
            .jsonPath(Json.CHAPTER_ID, 0).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 0).value(id -> introChapter.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 0).isEqualTo("Caro-Kann, Introduction")
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 0).isEqualTo(1)
            // chapter - Short variation
            .jsonPath(Json.CHAPTER_ID, 1).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 1).value(id -> shortChapter.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 1).isEqualTo("Caro-Kann, Short variation")
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 1).isEqualTo(1)
            // chapter - Karpov variation
            .jsonPath(Json.CHAPTER_ID, 2).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 2).value(id -> karpovChapter.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 2).isEqualTo("Caro-Kann, Karpov")
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 2).isEqualTo(1)
            // line - Short variation
            .jsonPath(Json.LINE_ID, 1).isNotEmpty()
            .jsonPath(Json.LINE_ID, 1).value(id -> shortLine1.set(id.toString()))
            .jsonPath(Json.LINE_CHAPTER_ID, 1).isEqualTo(shortChapter.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 1).isEqualTo(10)
            .jsonPath(Json.LINE_MOVE_UCI, 1, 0).isEqualTo("e2e4")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 1).isEqualTo("c7c6")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 2).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 3).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 4).isEqualTo("e4e5")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 5).isEqualTo("c8f5")
            .jsonPath(Json.LINE_MOVE_COMMENT, 1, 6).isEqualTo("Short 1")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 6).isEqualTo("g1f3")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 7).isEqualTo("e7e6")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 8).isEqualTo("f1e2")
            .jsonPath(Json.LINE_MOVE_UCI, 1, 9).isEqualTo("c6c5")
            .jsonPath(Json.LINE_BOX_ID, 1).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 1).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 1).isEmpty()
            // line - Short variation, transposition
            .jsonPath(Json.LINE_ID, 2).isNotEmpty()
            .jsonPath(Json.LINE_ID, 2).value(id -> shortLine2.set(id.toString()))
            .jsonPath(Json.LINE_CHAPTER_ID, 2).isEqualTo(shortChapter.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 2).isEqualTo(9)
            .jsonPath(Json.LINE_MOVE_UCI, 2, 0).isEqualTo("e2e4")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 1).isEqualTo("c7c6")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 2).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 3).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 4).isEqualTo("e4e5")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 5).isEqualTo("c8f5")
            .jsonPath(Json.LINE_MOVE_COMMENT, 2, 6).isEqualTo("Short 2")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 6).isEqualTo("f1e2")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 7).isEqualTo("e7e6")
            .jsonPath(Json.LINE_MOVE_UCI, 2, 8).isEqualTo("g1f3")
            .jsonPath(Json.LINE_BOX_ID, 2).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 2).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 2).isEmpty()
            // line - Karpov
            .jsonPath(Json.LINE_ID, 3).isNotEmpty()
            .jsonPath(Json.LINE_ID, 3).value(id -> karpovLine.set(id.toString()))
            .jsonPath(Json.LINE_CHAPTER_ID, 3).isEqualTo(karpovChapter.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 3).isEqualTo(8)
            .jsonPath(Json.LINE_MOVE_UCI, 3, 0).isEqualTo("e2e4")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 1).isEqualTo("c7c6")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 2).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 3).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 4).isEqualTo("b1c3")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 5).isEqualTo("d5e4")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 6).isEqualTo("c3e4")
            .jsonPath(Json.LINE_MOVE_COMMENT, 3, 7).isEqualTo("Karpov")
            .jsonPath(Json.LINE_MOVE_UCI, 3, 7).isEqualTo("b8d7")
            .jsonPath(Json.LINE_BOX_ID, 3).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 3).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 3).isEmpty());

        // toggle chapter introduction
        this.webTestClient.put()
            .uri(Api.TOGGLE_CHAPTER)
            .bodyValue(ToggleChapterRequest.builder()
                .bookId(bookId.get())
                .chapterId(UUID.fromString(introChapter.get()))
                .enabled(false)
                .build())
            .exchange()
            .expectStatus().isNoContent();

        this.runAndAssertNextLine(
            bookId.get(),
            shortLine1.get(), shortChapter.get(),
            shortLine1Ucis,
            0, null,
            null);

        lastTraining.put(
            shortLine1.get(),
            this.runAndAssertSetResult(
                bookId.get(), shortLine1.get(), true,
                shortChapter.get(), 1, 0));

        this.runAndAssertNextLine(
            bookId.get(),
            shortLine2.get(), shortChapter.get(),
            shortLine2Ucis,
            0, null,
            null);

        lastTraining.put(
            shortLine2.get(),
            this.runAndAssertSetResult(
                bookId.get(), shortLine2.get(), true,
                shortChapter.get(), 1, 0));

        this.runAndAssertNextLine(
            bookId.get(),
            karpovLine.get(), karpovChapter.get(),
            karpovLineUcis,
            0, null,
            null);

        lastTraining.put(
            karpovLine.get(),
            this.runAndAssertSetResult(
                bookId.get(), karpovLine.get(), false,
                karpovChapter.get(), 0, 0));

        this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, bookId.get())
            .exchange()
            .expectStatus().isNoContent();

        this.webTestClient
            .post()
            .uri(Api.NEXT_CALENDAR_SLOT)
            .bodyValue(NextCalendarSlotRequest.builder()
                .bookId(bookId.get())
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody().isEmpty();

        this.runAndAssertNextLine(
            bookId.get(),
            karpovLine.get(), karpovChapter.get(),
            karpovLineUcis,
            0, 0,
            lastTraining.get(karpovLine.get()));

        this.runAndAssertSetResult(
            bookId.get(), karpovLine.get(), true,
            karpovChapter.get(), 1, 1);

        this.runAndAssertNextLine(
            bookId.get(),
            shortLine1.get(), shortChapter.get(),
            shortLine1Ucis,
            1, 0,
            lastTraining.get(shortLine1.get()));
    }

    private void runAndAssertNextLine(final UUID bookId,
                                      final String expectedLineId,
                                      final String expectedChapterId,
                                      final List<String> expectedUcis,
                                      final int expectedBoxId,
                                      final Integer expectedLastSlot,
                                      final TrainingInstants trainingInstants) {

        WebTestClient.BodyContentSpec spec = this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, bookId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(Json.NEXT_LINE_ID).isEqualTo(expectedLineId)
            .jsonPath(Json.NEXT_LINE_CHAPTER_ID).isEqualTo(expectedChapterId)
            .jsonPath(Json.NEXT_LINE_MOVES_SIZE).isEqualTo(expectedUcis.size())
            .jsonPath(Json.NEXT_LINE_BOX_ID).isEqualTo(expectedBoxId)
            .jsonPath(Json.NEXT_LINE_LAST_TRAINING).value(
                d -> Optional
                    .ofNullable(trainingInstants)
                    .ifPresentOrElse(
                        lastTrainingInstants ->
                            assertThat(Instant.parse(d.toString())).isBetween(
                                lastTrainingInstants.start(),
                                lastTrainingInstants.end()),
                        () -> assertThat(d).isNull()))
            .jsonPath(Json.NEXT_LINE_LAST_CALENDAR_SLOT).isEqualTo(expectedLastSlot);

        for (int i = 0; i < expectedUcis.size(); i++) {
            spec = spec
                .jsonPath(Json.NEXT_LINE_MOVE_UCI, i)
                .isEqualTo(expectedUcis.get(i));
        }
    }

    private TrainingInstants runAndAssertSetResult(
        final UUID bookId,
        final String lineId,
        final boolean result,
        final String expectedChapterId,
        final int expectedBoxId,
        final int expectedLastSlot) {

        final Instant beforeSettingResult = Instant.now();
        this.webTestClient
            .post()
            .uri(Api.SET_RESULT)
            .bodyValue(TrainingResultRequest.builder()
                .bookId(bookId)
                .lineId(UUID.fromString(lineId))
                .result(result)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(Json.NEXT_LINE_ID).isEqualTo(lineId)
            .jsonPath(Json.NEXT_LINE_CHAPTER_ID).isEqualTo(expectedChapterId)
            .jsonPath(Json.NEXT_LINE_BOX_ID).isEqualTo(expectedBoxId)
            .jsonPath(Json.NEXT_LINE_LAST_TRAINING).value(
                d -> assertThat(Instant.parse(d.toString()))
                    .isBetween(beforeSettingResult, Instant.now()))
            .jsonPath(Json.NEXT_LINE_LAST_CALENDAR_SLOT).isEqualTo(expectedLastSlot);

        return new TrainingInstants(beforeSettingResult, Instant.now());
    }

}
