package io.github.fifi2.chessmorize.controller.api;

import io.github.fifi2.chessmorize.controller.api.dto.TrainingResultRequest;
import io.github.fifi2.chessmorize.error.exception.BookNotFoundException;
import io.github.fifi2.chessmorize.error.exception.LineNotFoundException;
import io.github.fifi2.chessmorize.error.exception.NoTrainingLineException;
import io.github.fifi2.chessmorize.helper.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.model.Book;
import io.github.fifi2.chessmorize.model.Line;
import io.github.fifi2.chessmorize.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.github.fifi2.chessmorize.helper.Constants.Api;

class TrainingControllerTest extends AbstractSpringBootTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TrainingService trainingService;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getNextLine(final boolean isLineFound) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.trainingService.getNextLine(bookId))
            .thenReturn(isLineFound
                ? Mono.just(Line.builder().build())
                : Mono.error(new NoTrainingLineException(bookId)));

        final WebTestClient.ResponseSpec getExchange = this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, bookId)
            .exchange();

        if (isLineFound)
            getExchange.expectStatus().isOk();
        else
            getExchange.expectStatus().isNoContent();
    }

    @Test
    void getNextLine_withBadInput() {

        this.webTestClient
            .get()
            .uri(Api.NEXT_LINE, "bad")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void setLineResult() {

        Mockito
            .when(this.trainingService.setLineResult(
                Mockito.any(),
                Mockito.any(),
                Mockito.anyBoolean()
            ))
            .thenReturn(Mono.just(Line.builder().build()));

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT, UUID.randomUUID())
            .bodyValue(TrainingResultRequest.builder()
                .lineId(UUID.randomUUID())
                .result(true)
                .build())
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void setLineResult_withNoInput() {

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT, UUID.randomUUID())
            .exchange()
            .expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', textBlock = """
        true  | true  | false
        true  | false | true
        false | true  | true
        false | false | false
        """)
    void setLineResult_withBadInput(final boolean bookIdStatus,
                                    final boolean lineIdStatus,
                                    final boolean resultStatus) {

        final TrainingResultRequest request = TrainingResultRequest.builder()
            .lineId(lineIdStatus
                ? UUID.randomUUID()
                : null)
            .build();

        if (resultStatus)
            request.setResult(true);

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT, bookIdStatus
                ? UUID.randomUUID()
                : "bad")
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void setLineResult_withNoBookFound() {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.trainingService.setLineResult(
                Mockito.eq(bookId),
                Mockito.any(),
                Mockito.anyBoolean()))
            .thenReturn(Mono.error(new BookNotFoundException(bookId)));

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT, bookId)
            .bodyValue(TrainingResultRequest.builder()
                .lineId(UUID.randomUUID())
                .result(true)
                .build())
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void setLineResult_withNoLineFound() {

        final UUID bookId = UUID.randomUUID();
        final UUID lineId = UUID.randomUUID();

        Mockito
            .when(this.trainingService.setLineResult(
                Mockito.eq(bookId),
                Mockito.eq(lineId),
                Mockito.anyBoolean()))
            .thenReturn(Mono.error(new LineNotFoundException(bookId, lineId)));

        this.webTestClient
            .post()
            .uri(Api.SET_RESULT, bookId)
            .bodyValue(TrainingResultRequest.builder()
                .lineId(lineId)
                .result(true)
                .build())
            .exchange()
            .expectStatus().isNotFound();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void nextCalendarSlot(final boolean isBookFound) {

        final UUID bookId = UUID.randomUUID();

        Mockito
            .when(this.trainingService.nextCalendarSlot(Mockito.any()))
            .thenReturn(isBookFound
                ? Mono.just(Book.builder().id(bookId).build())
                : Mono.error(new BookNotFoundException(bookId)));

        final WebTestClient.ResponseSpec postExchange = this.webTestClient
            .post()
            .uri(Api.NEXT_SLOT, bookId)
            .exchange();

        if (isBookFound)
            postExchange.expectStatus().isOk().expectBody().isEmpty();
        else
            postExchange.expectStatus().isNotFound();
    }

    @Test
    void nextCalendarSlot_withBadInput() {

        this.webTestClient
            .post()
            .uri(Api.NEXT_SLOT, "bad")
            .exchange()
            .expectStatus().isBadRequest();
    }

}
