package io.github.fifi2.chessmorize.controller.api;

import io.github.fifi2.chessmorize.controller.api.dto.BookCreationRequest;
import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.error.exception.BookSerDeException;
import io.github.fifi2.chessmorize.error.exception.lichess.Lichess5xxException;
import io.github.fifi2.chessmorize.error.exception.lichess.LichessNotFoundException;
import io.github.fifi2.chessmorize.error.exception.lichess.LichessTimeoutException;
import io.github.fifi2.chessmorize.error.exception.pgn.PgnException;
import io.github.fifi2.chessmorize.helper.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.stream.Stream;

import static io.github.fifi2.chessmorize.helper.Constants.Api;

class BookControllerTest extends AbstractSpringBootTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @DisplayName("Recognize bad request:")
    @ParameterizedTest(name = "{index}: having studyId={0}")
    @CsvSource(delimiter = '|', nullValues = "null", textBlock = """
        null
        ''
        too-long-id
        """)
    void postBook_withInvalidInput(final String studyId) {

        this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId(studyId)
                .build())
            .exchange()
            .expectStatus().isBadRequest();
    }

    static Stream<Arguments> postBook_withException() {

        return Stream.of(
            Arguments.of(
                new LichessNotFoundException("study-id", null),
                HttpStatus.BAD_REQUEST),
            Arguments.of(
                new LichessTimeoutException(HttpStatus.REQUEST_TIMEOUT, null),
                HttpStatus.GATEWAY_TIMEOUT),
            Arguments.of(
                new Lichess5xxException(
                    "5xx",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    null),
                HttpStatus.BAD_GATEWAY),
            Arguments.of(
                new PgnException("study-id", null),
                HttpStatus.INTERNAL_SERVER_ERROR),
            Arguments.of(
                new BookSerDeException("Json serialization failure", null),
                HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ParameterizedTest
    @MethodSource
    void postBook_withException(final Throwable mockedException,
                                final HttpStatus expectedStatus) {

        Mockito
            .when(this.bookService.createBook(Mockito.anyString()))
            .thenReturn(Mono.error(mockedException));

        this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId("study-id")
                .build())
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

    @Test
    void postBook() {

        final String studyId = "study-id";
        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.bookService.createBook(studyId))
            .thenReturn(Mono.just(Book.builder()
                .id(bookId)
                .studyId(studyId)
                .build()));

        this.webTestClient
            .post()
            .uri(Api.BOOKS)
            .bodyValue(BookCreationRequest.builder()
                .studyId(studyId)
                .build())
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().location(Api.BOOKS + "/" + bookId);
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        true  | OK
        false | NOT_FOUND
        """)
    void getBook(final boolean existingBook,
                 final HttpStatus expectedStatus) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.bookService.getOneBook(bookId))
            .thenReturn(existingBook
                ? Mono.just(Book.builder().id(bookId).build())
                : Mono.error(new BookNotFoundException(bookId)));

        this.webTestClient
            .get()
            .uri(Api.BOOK, bookId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

    static Stream<Arguments> getBook_withException() {

        return Stream.of(
            Arguments.of(
                new BookSerDeException("Json deserialization failture", null),
                HttpStatus.INTERNAL_SERVER_ERROR),
            Arguments.of(
                new BookNotFoundException(UUID.randomUUID()),
                HttpStatus.NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource
    void getBook_withException(final Throwable mockedException,
                               final HttpStatus expectedStatus) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.bookService.getOneBook(bookId))
            .thenReturn(Mono.error(mockedException));

        this.webTestClient
            .get()
            .uri(Api.BOOK, bookId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

    @Test
    void getAllBooks() {

        Mockito
            .when(this.bookService.getAllBooks())
            .thenReturn(Flux.just(Book.builder().build()));

        this.webTestClient
            .get()
            .uri(Api.BOOKS)
            .exchange()
            .expectStatus().isOk();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        true  | NO_CONTENT
        false | NOT_FOUND
        """)
    void deleteBook(final boolean isDeletionOk,
                    final HttpStatus expectedStatus) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.bookService.deleteOneBook(bookId))
            .thenReturn(isDeletionOk
                ? Mono.just(bookId)
                : Mono.error(new BookNotFoundException(bookId)));

        this.webTestClient
            .delete()
            .uri(Api.BOOK, bookId)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus);
    }

}
