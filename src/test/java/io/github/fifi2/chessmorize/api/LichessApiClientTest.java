package io.github.fifi2.chessmorize.api;

import io.github.fifi2.chessmorize.AbstractSpringBootTest;
import io.github.fifi2.chessmorize.error.exception.lichess.LichessException;
import io.github.fifi2.chessmorize.helper.LichessMock;
import io.github.fifi2.chessmorize.helper.LichessMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.test.StepVerifier;

class LichessApiClientTest extends AbstractSpringBootTest {

    private static final String STUDY_ID = "whatever";

    @Autowired
    private LichessApiClient lichessApiClient;

    @DisplayName("Get a study from Lichess")
    @Test
    @ExtendWith(LichessMockExtension.class)
    void getStudyPGN_withOkResponse(final LichessMock lichessMock) {

        final String pgn = "fake pgn for test";
        lichessMock.mockResponse(pgn);

        StepVerifier
            .create(this.lichessApiClient.getStudyPGN(STUDY_ID))
            .expectNextMatches(pgn::equals)
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
    @ExtendWith(LichessMockExtension.class)
    void getStudyPGN_withException(final HttpStatus status,
                                   final String exceptionName,
                                   final LichessMock lichessMock) {

        lichessMock.mockError(status);

        StepVerifier
            .create(this.lichessApiClient.getStudyPGN(STUDY_ID))
            .expectErrorMatches(error -> error instanceof LichessException e
                && status.equals(e.getStatus())
                && exceptionName.equals(error.getClass().getSimpleName()))
            .verify();
    }

}
