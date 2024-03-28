package io.github.fifi2.chessmorize.it;

import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.helper.AbstractLichessTest;
import io.github.fifi2.chessmorize.helper.ObjectWrapper;
import io.github.fifi2.chessmorize.helper.converter.StringToList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.helper.Constants.Api;
import static io.github.fifi2.chessmorize.helper.Constants.Json;

@TestPropertySource(properties = {
    "resilience4j.retry.configs.default.max-attempts=2"
})
class BookIntegrationTest extends AbstractLichessTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void bookApi_nominal() {

        final String studyId = "study-id";
        final ObjectWrapper<String> bookId = new ObjectWrapper<>();
        final ObjectWrapper<String> chapterId = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId1 = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId2 = new ObjectWrapper<>();
        final ObjectWrapper<String> moveId3 = new ObjectWrapper<>();

        this.lichessMockResponse("""
            [Event "White: Queen's gambit"]
                        
            1. d4 d5 2. c4 *
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
                .jsonPath(Json.ID).value(id -> bookId.set(id.toString()));

        // get the book
        final WebTestClient.BodyContentSpec getBody =
            this.webTestClient
                .get()
                .uri(Api.BOOK, bookId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(Json.ID).isEqualTo(bookId.get());

        // assert POST and GET responses
        Stream.of(postBody, getBody).forEach(body -> body
            .jsonPath(Json.STUDY_ID).isEqualTo(studyId)
            .jsonPath(Json.NAME).isEqualTo("White")
            .jsonPath(Json.CHAPTERS_SIZE).isEqualTo(1)
            .jsonPath(Json.CHAPTER_ID, 0).isNotEmpty()
            .jsonPath(Json.CHAPTER_ID, 0).value(id -> chapterId.set(id.toString()))
            .jsonPath(Json.CHAPTER_TITLE, 0).isEqualTo("Queen's gambit")
            .jsonPath(Json.CHAPTER_MOVES_SIZE, 0).isEqualTo(1)
            .jsonPath(Json.MOVE(0, 0) + ".id").value(id -> moveId1.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0) + ".san").isEqualTo("d4")
            .jsonPath(Json.MOVE(0, 0) + ".uci").isEqualTo("d2d4")
            .jsonPath(Json.MOVE(0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0) + ".id").value(id -> moveId2.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0) + ".san").isEqualTo("d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".uci").isEqualTo("d7d5")
            .jsonPath(Json.MOVE(0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".id").value(id -> moveId3.set(id.toString()))
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".san").isEqualTo("c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".uci").isEqualTo("c2c4")
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nag").isEmpty()
            .jsonPath(Json.MOVE(0, 0, 0, 0) + ".nextMoves").isEmpty()
            .jsonPath(Json.LINES_SIZE).isEqualTo(1)
            .jsonPath(Json.LINE_ID, 0).isNotEmpty()
            .jsonPath(Json.LINE_CHAPTER_ID, 0).isEqualTo(chapterId.get())
            .jsonPath(Json.LINE_MOVES_SIZE, 0).isEqualTo(3)
            .jsonPath(Json.LINE_MOVE_UCI, 0, 0).isEqualTo("d2d4")
            .jsonPath(Json.LINE_MOVE_ID, 0, 0).isEqualTo(moveId1.get())
            .jsonPath(Json.LINE_MOVE_UCI, 0, 1).isEqualTo("d7d5")
            .jsonPath(Json.LINE_MOVE_ID, 0, 1).isEqualTo(moveId2.get())
            .jsonPath(Json.LINE_MOVE_UCI, 0, 2).isEqualTo("c2c4")
            .jsonPath(Json.LINE_MOVE_ID, 0, 2).isEqualTo(moveId3.get())
            .jsonPath(Json.LINE_BOX_ID, 0).isEqualTo(0)
            .jsonPath(Json.LINE_LAST_TRAINING, 0).isEmpty()
            .jsonPath(Json.LINE_LAST_CALENDAR_SLOT, 0).isEmpty()
            .jsonPath(Json.CALENDAR_SLOT).isEqualTo(0));

        // get all books
        this.webTestClient
            .get()
            .uri(Api.BOOKS)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$.[0].id").isEqualTo(bookId.get())
            .jsonPath("$.[0].studyId").isEqualTo(studyId)
            .jsonPath("$.[0].name").isEqualTo("White")
            .jsonPath("$.[0].chapters").doesNotExist()
            .jsonPath("$.[0].lines").doesNotExist();

        // delete the book
        this.webTestClient
            .delete()
            .uri(Api.BOOK, bookId.get())
            .exchange()
            .expectStatus().isNoContent()
            .expectBody().isEmpty();

        // check the book can't be found anymore
        this.webTestClient
            .get()
            .uri(Api.BOOK, bookId.get())
            .exchange()
            .expectStatus().isNotFound();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        OK                                 | CREATED
        REQUEST_TIMEOUT,OK                 | CREATED
        REQUEST_TIMEOUT,REQUEST_TIMEOUT,OK | GATEWAY_TIMEOUT
        """)
    void bookApi_withRetry(
        @ConvertWith(StringToList.class) final List<String> lichessStatuses,
        final HttpStatus expectedStatus) {

        final ObjectWrapper<String> bookId = new ObjectWrapper<>();

        lichessStatuses
            .stream()
            .map(HttpStatus::valueOf)
            .forEach(status -> {
                if (status == HttpStatus.OK) {
                    this.lichessMockResponse("""
                        [Event "Black: Caro-Kann"]

                        1. e4 c6 2. d4 *
                        """);
                } else {
                    this.lichessMockError(HttpStatus.REQUEST_TIMEOUT);
                }
            });

        // POST /api/book
        final WebTestClient.ResponseSpec responseSpec = this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId("randomId")
                .build())
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);

        if (expectedStatus == HttpStatus.CREATED) {
            responseSpec
                .expectBody()
                .jsonPath(Json.ID).value(id -> bookId.set(id.toString()));

            // GET /api/book/{bookId}
            this.webTestClient
                .get()
                .uri(Api.BOOK, bookId.get())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath(Json.ID, bookId.get());
        }
    }

}
