package io.github.fifi2.chessmorize.it;

import io.github.fifi2.chessmorize.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.controller.api.dto.ToggleChapterRequest;
import io.github.fifi2.chessmorize.helper.LichessMock;
import io.github.fifi2.chessmorize.helper.LichessMockExtension;
import io.github.fifi2.chessmorize.helper.ObjectWrapper;
import io.github.fifi2.chessmorize.helper.builder.BookBuilder;
import io.github.fifi2.chessmorize.helper.converter.StringToList;
import io.github.fifi2.chessmorize.model.*;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.UUID;

import static io.github.fifi2.chessmorize.helper.Constants.Api;
import static io.github.fifi2.chessmorize.helper.Constants.Json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@TestPropertySource(properties = {
    "resilience4j.retry.configs.default.max-attempts=2",
})
class BookApiTest extends AbstractSpringBootTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @ExtendWith(LichessMockExtension.class)
    void postBook(final LichessMock lichessMock) {

        final String studyId = "study-id";
        final ObjectWrapper<String> chapterId = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId1 = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId2 = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId3 = new ObjectWrapper<>();

        lichessMock.mockResponse("""
            [Event "White: Queen's gambit"]
            [StudyName "White"]
            [ChapterName "Queen's gambit"]
            
            1. d4 { Queen's opening } d5 2. c4! *
            """);

        // create a book
        this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId(studyId)
                .color(Color.WHITE)
                .build())
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath(Json.ID).value(id ->
                assertThatCode(() -> UUID.fromString(id.toString()))
                    .doesNotThrowAnyException())
            .jsonPath(Json.STUDY_ID).isEqualTo(studyId)
            .jsonPath(Json.NAME).isEqualTo("White")
            .jsonPath(Json.COLOR).isEqualTo(Color.WHITE.name())
            .jsonPath(Json.CHAPTERS_SIZE).isEqualTo(1)
            .jsonPath(Json.CHAPTER_ID, 0).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 0).value(id -> chapterId.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 0).isEqualTo("Queen's gambit")
            .jsonPath(Json.CHAPTER_ENABLED, 0).isEqualTo(true)
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 0).isEqualTo(1)
            .jsonPath(Json.MOVE(0, 0) + ".id").value(id -> moveId1.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0) + ".san").isEqualTo("d4")
            .jsonPath(Json.MOVE(0, 0) + ".comment").isEqualTo("Queen's opening")
            .jsonPath(Json.MOVE(0, 0) + ".uci").isEqualTo("d2d4")
            .jsonPath(Json.MOVE(0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".id").value(id -> moveId2.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0) + ".san").isEqualTo("d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".uci").isEqualTo("d7d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".id").value(id -> moveId3.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".san").isEqualTo("c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".uci").isEqualTo("c2c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nag").isEqualTo(Nag.GOOD_MOVE.name())
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nextMoves").isEmpty()
            .jsonPath(Json.LINES_SIZE).isEqualTo(1)
            .jsonPath(Json.LINE_ID, 0).isNotEmpty()
            .jsonPath(Json.LINE_CHAPTER_ID, 0).isEqualTo(chapterId.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 0).isEqualTo(3)
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 0).isEqualTo("Queen's opening")
            .jsonPath(Json.LINE_MOVE_SAN, 0, 0).isEqualTo("d4")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 0).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_NAG, 0, 0).doesNotExist()
            .jsonPath(Json.LINE_MOVE_ID, 0, 0).isEqualTo(moveId1.get())
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 1).isEmpty()
            .jsonPath(Json.LINE_MOVE_SAN, 0, 1).isEqualTo("d5")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 1).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_NAG, 0, 1).doesNotExist()
            .jsonPath(Json.LINE_MOVE_ID, 0, 1).isEqualTo(moveId2.get())
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 2).isEmpty()
            .jsonPath(Json.LINE_MOVE_SAN, 0, 2).isEqualTo("c4")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 2).isEqualTo("c2c4")
            .jsonPath(Json.LINE_MOVE_NAG, 0, 2).isEqualTo(Nag.GOOD_MOVE.name())
            .jsonPath(Json.LINE_MOVE_ID, 0, 2).isEqualTo(moveId3.get())
            .jsonPath(Json.LINE_BOX_ID, 0).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 0).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 0).isEmpty()
            .jsonPath(Json.CALENDAR_SLOT).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        OK                                 | CREATED
        REQUEST_TIMEOUT,OK                 | CREATED
        REQUEST_TIMEOUT,REQUEST_TIMEOUT,OK | GATEWAY_TIMEOUT
        """)
    @ExtendWith(LichessMockExtension.class)
    void postBook_withRetry(
        @ConvertWith(StringToList.class) final List<String> lichessStatuses,
        final HttpStatus expectedStatus,
        final LichessMock lichessMock) {

        final String studyId = "ramdomId";

        lichessStatuses
            .stream()
            .map(HttpStatus::valueOf)
            .forEach(status -> {
                if (status == HttpStatus.OK)
                    lichessMock.mockResponse("""
                        [Event "Black: Caro-Kann"]
                        [StudyName "Black"]
                        [ChapterName "Caro-Kann"]
                        
                        1. e4 c6 2. d4 *
                        """);
                else
                    lichessMock.mockError(HttpStatus.REQUEST_TIMEOUT);
            });

        // POST /api/book
        final WebTestClient.ResponseSpec responseSpec = this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId(studyId)
                .color(Color.BLACK)
                .build())
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);

        if (expectedStatus != HttpStatus.CREATED)
            return;

        final ObjectWrapper<String> chapterId = new ObjectWrapper<>();
        final ObjectWrapper<String> move1Id = new ObjectWrapper<>();
        final ObjectWrapper<String> move2Id = new ObjectWrapper<>();
        final ObjectWrapper<String> move3Id = new ObjectWrapper<>();
        responseSpec
            .expectBody()
            .jsonPath(Json.ID).value(id ->
                assertThatCode(() -> UUID.fromString(id.toString()))
                    .doesNotThrowAnyException())
            .jsonPath(Json.STUDY_ID).isEqualTo(studyId)
            .jsonPath(Json.NAME).isEqualTo("Black")
            .jsonPath(Json.COLOR).isEqualTo(Color.BLACK.name())
            .jsonPath(Json.CHAPTERS_SIZE).isEqualTo(1)
            .jsonPath(Json.CHAPTER_ID, 0).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 0).value(id -> chapterId.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 0).isEqualTo("Caro-Kann")
            .jsonPath(Json.CHAPTER_ENABLED, 0).isEqualTo(true)
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 0).isEqualTo(1)
            .jsonPath(Json.MOVE(0, 0) + ".id").value(id -> move1Id.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0) + ".san").isEqualTo("e4")
            .jsonPath(Json.MOVE(0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0) + ".uci").isEqualTo("e2e4")
            .jsonPath(Json.MOVE(0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".id").value(id -> move2Id.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0) + ".san").isEqualTo("c6")
            .jsonPath(Json.MOVE(0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".uci").isEqualTo("c7c6")
            .jsonPath(Json.MOVE(0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".id").value(id -> move3Id.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".san").isEqualTo("d4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".uci").isEqualTo("d2d4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nextMoves").isEmpty()
            .jsonPath(Json.LINES_SIZE).isEqualTo(1)
            .jsonPath(Json.LINE_ID, 0).isNotEmpty()
            .jsonPath(Json.LINE_CHAPTER_ID, 0).isEqualTo(chapterId.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 0).isEqualTo(2)
            .jsonPath(Json.LINE_MOVE_ID, 0, 0).isEqualTo(move1Id.get())
            .jsonPath(Json.LINE_MOVE_SAN, 0, 0).isEqualTo("e4")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 0).isEqualTo("e2e4")
            .jsonPath(Json.LINE_MOVE_NAG, 0, 0).doesNotExist()
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 0).isEmpty()
            .jsonPath(Json.LINE_MOVE_ID, 0, 1).isEqualTo(move2Id.get())
            .jsonPath(Json.LINE_MOVE_SAN, 0, 1).isEqualTo("c6")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 1).isEqualTo("c7c6")
            .jsonPath(Json.LINE_MOVE_NAG, 0, 1).doesNotExist()
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 1).isEmpty()
            .jsonPath(Json.LINE_BOX_ID, 0).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 0).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 0).isEmpty()
            .jsonPath(Json.CALENDAR_SLOT).isEqualTo(0);
    }

    @Test
    void getBook_notFound() {

        this.webTestClient
            .get()
            .uri(Api.BOOK, UUID.randomUUID())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void getBook() {

        final UUID bookId = UUID.randomUUID();
        final String bookName = "White";
        final UUID chapterId = UUID.randomUUID();
        final UUID move1Id = UUID.randomUUID();
        final UUID move2Id = UUID.randomUUID();
        final UUID move3Id = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .name(bookName)
            .color(Color.WHITE)
            .withChapter(chapter -> chapter
                .id(chapterId)
                .title("Queen's gambit")
                .withNextMove(m1 -> m1
                    .id(move1Id)
                    .san("d4")
                    .uci("d2d4")
                    .comment("Queen's opening")
                    .color(Color.WHITE)
                    .withNextMove(m2 -> m2
                        .id(move2Id)
                        .san("d5")
                        .uci("d7d5")
                        .color(Color.BLACK)
                        .withNextMove(m3 -> m3
                            .id(move3Id)
                            .san("c4")
                            .uci("c2c4")
                            .color(Color.WHITE)))))
            .withLine(line -> line
                .chapterId(chapterId)
                .boxId(0)
                .lastTraining(null)
                .lastCalendarSlot(null)
                .withMove(move -> move
                    .moveId(move1Id)
                    .uci("d2d4")
                    .comment("Queen's opening"))
                .withMove(move -> move
                    .moveId(move2Id)
                    .uci("d7d5"))
                .withMove(move -> move
                    .moveId(move3Id)
                    .uci("c2c4")))
            .calendarSlot(0)
            .build());

        this.webTestClient
            .get()
            .uri(Api.BOOK, bookId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(Json.ID).isEqualTo(bookId.toString())
            .jsonPath(Json.NAME).isEqualTo(bookName)
            .jsonPath(Json.COLOR).isEqualTo(Color.WHITE.name())
            .jsonPath(Json.CHAPTERS_SIZE).isEqualTo(1)
            .jsonPath(Json.CHAPTER_ID, 0).isEqualTo(chapterId.toString())
            .jsonPath(Json.CHAPTER_TITLE, 0).isEqualTo("Queen's gambit")
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 0).isEqualTo(1)
            .jsonPath(Json.MOVE(0, 0) + ".id").isEqualTo(move1Id.toString())
            .jsonPath(Json.MOVE(0, 0) + ".san").isEqualTo("d4")
            .jsonPath(Json.MOVE(0, 0) + ".comment").isEqualTo("Queen's opening")
            .jsonPath(Json.MOVE(0, 0) + ".uci").isEqualTo("d2d4")
            .jsonPath(Json.MOVE(0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0) + ".color").isEqualTo(Color.WHITE.name())
            .jsonPath(Json.MOVE(0, 0, 0) + ".id").isEqualTo(move2Id.toString())
            .jsonPath(Json.MOVE(0, 0, 0) + ".san").isEqualTo("d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".uci").isEqualTo("d7d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".color").isEqualTo(Color.BLACK.name())
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".id").isEqualTo(move3Id.toString())
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".san").isEqualTo("c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".comment").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".uci").isEqualTo("c2c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".color").isEqualTo(Color.WHITE.name())
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nextMoves").isEmpty()
            .jsonPath(Json.LINES_SIZE).isEqualTo(1)
            .jsonPath(Json.LINE_ID, 0).isNotEmpty()
            .jsonPath(Json.LINE_CHAPTER_ID, 0).isEqualTo(chapterId.toString())
            .jsonPath(Json.LINE_MOVES_SIZE, 0).isEqualTo(3)
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 0).isEqualTo("Queen's opening")
            .jsonPath(Json.LINE_MOVE_UCI, 0, 0).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_ID, 0, 0).isEqualTo(move1Id.toString())
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 1).isEmpty()
            .jsonPath(Json.LINE_MOVE_UCI, 0, 1).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_ID, 0, 1).isEqualTo(move2Id.toString())
            .jsonPath(Json.LINE_MOVE_COMMENT, 0, 2).isEmpty()
            .jsonPath(Json.LINE_MOVE_UCI, 0, 2).isEqualTo("c2c4")
            .jsonPath(Json.LINE_MOVE_ID, 0, 2).isEqualTo(move3Id.toString())
            .jsonPath(Json.LINE_BOX_ID, 0).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 0).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 0).isEmpty()
            .jsonPath(Json.CALENDAR_SLOT).isEqualTo(0);
    }

    @Test
    void getAllBooks_withNoBook() {

        this.cleanDatabase();

        this.webTestClient
            .get()
            .uri(Api.BOOKS)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody();
    }

    @Test
    void getAllBooks() {

        final UUID book1Id = UUID.randomUUID();
        final UUID book2Id = UUID.randomUUID();

        this.cleanDatabase();

        this.saveBook(BookBuilder.builder()
            .id(book1Id)
            .name("White")
            .color(Color.WHITE)
            .studyId("random-1")
            .withChapter(chapter -> chapter
                .title("Queen's gambit"))
            .build());

        this.saveBook(BookBuilder.builder()
            .id(book2Id)
            .name("Black")
            .color(Color.BLACK)
            .studyId("random-2")
            .withChapter(chapter -> chapter
                .title("Caro-Kann"))
            .build());

        this.webTestClient
            .get()
            .uri(Api.BOOKS)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$.[0].id").isEqualTo(book1Id.toString())
            .jsonPath("$.[0].studyId").doesNotExist()
            .jsonPath("$.[0].name").isEqualTo("White")
            .jsonPath("$.[0].color").isEqualTo(Color.WHITE.name())
            .jsonPath("$.[0].chapters").doesNotExist()
            .jsonPath("$.[0].lines").doesNotExist()
            .jsonPath("$.[1].id").isEqualTo(book2Id.toString())
            .jsonPath("$.[1].studyId").doesNotExist()
            .jsonPath("$.[1].name").isEqualTo("Black")
            .jsonPath("$.[1].color").isEqualTo(Color.BLACK.name())
            .jsonPath("$.[1].chapters").doesNotExist()
            .jsonPath("$.[1].lines").doesNotExist();
    }

    @Test
    void deleteBook_notFound() {

        this.webTestClient
            .delete()
            .uri(Api.BOOK, UUID.randomUUID())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void deleteBook() {

        final UUID bookId = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .name("White")
            .color(Color.WHITE)
            .build());

        this.webTestClient
            .delete()
            .uri(Api.BOOK, bookId)
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();

        this.assertNoBook(bookId);
    }

    @Test
    void toggleChapter() {

        final UUID bookId = UUID.randomUUID();
        final UUID chapter1Id = UUID.randomUUID();
        final UUID line1Id = UUID.randomUUID();
        final UUID chapter2Id = UUID.randomUUID();
        final UUID line2Id = UUID.randomUUID();

        this.saveBook(BookBuilder.builder()
            .id(bookId)
            .name("White")
            .color(Color.WHITE)
            .withChapter(chapter -> chapter
                .id(chapter1Id)
                .title("Queen's gambit - Introduction")
                .enabled(true))
            .withChapter(chapter -> chapter
                .id(chapter2Id)
                .title("Queen's gambit - Exchange variation")
                .enabled(true))
            .withLine(line -> line
                .id(line1Id)
                .chapterId(chapter1Id))
            .withLine(line -> line
                .id(line2Id)
                .chapterId(chapter2Id))
            .build());

        // toggle the first chapter
        this.webTestClient
            .put()
            .uri(Api.TOGGLE_CHAPTER)
            .bodyValue(ToggleChapterRequest.builder()
                .bookId(bookId)
                .chapterId(chapter1Id)
                .enabled(false)
                .build())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();

        final Book book = this.getBook(bookId);

        assertThat(book.getChapters())
            .hasSize(2)
            .extracting(Chapter::getId, Chapter::isEnabled)
            .containsExactly(
                Tuple.tuple(chapter1Id, false),
                Tuple.tuple(chapter2Id, true));

        assertThat(book.getLines())
            .hasSize(1)
            .extracting(Line::getChapterId)
            .doesNotContain(chapter1Id);
    }

}
