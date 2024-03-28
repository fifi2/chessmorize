package io.github.fifi2.chessmorize.api;

import io.github.fifi2.chessmorize.error.exception.lichess.LichessException;
import io.github.fifi2.chessmorize.helper.AbstractLichessTest;
import io.github.fifi2.chessmorize.api.LichessApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

class LichessApiClientTest extends AbstractLichessTest {

    private static final String STUDY_ID = "whatever";

    @Autowired
    private LichessApiClient lichessApiClient;

    @DisplayName("Get a study from Lichess")
    @Test
    void getStudyPGN_withOkResponse() {

        this.lichessMockResponse("fake pgn for test");

        StepVerifier
            .create(this.lichessApiClient.getStudyPGN(STUDY_ID))
            .expectNextMatches("fake pgn for test"::equals)
            .verifyComplete();
    }

    @DisplayName("Throw expected error in case of exception:")
    @ParameterizedTest(name = "{index}: when exception is {0}")
    @CsvSource(delimiter = '|', textBlock = """
        BAD_REQUEST         | Lichess4xxException
        NOT_FOUND           | LichessNotFoundException
        REQUEST_TIMEOUT     | LichessTimeoutException
        SERVICE_UNAVAILABLE | Lichess5xxException
        """)
    void getStudyPGN_withException(final HttpStatus status,
                                   final String exceptionName) {

        this.lichessMockError(status);

        StepVerifier
            .create(this.lichessApiClient.getStudyPGN(STUDY_ID))
            .expectErrorMatches(error -> error instanceof LichessException e
                && status.equals(e.getStatus())
                && exceptionName.equals(error.getClass().getSimpleName()))
            .verify();
    }

}
