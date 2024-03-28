package io.github.fifi2.chessmorize.helper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public abstract class AbstractLichessTest extends AbstractSpringBootTest {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_CHESS_PGN = "application/x-chess-pgn";
    private static final String TEXT_PLAIN = "text/plain";

    protected MockWebServer lichessMock;

    @BeforeEach
    void beforeEach() throws IOException {

        this.lichessMock = new MockWebServer();
        this.lichessMock.start(8081);
    }

    @AfterEach
    void afterEach() throws IOException {

        this.lichessMock.shutdown();
    }

    protected void lichessMockResponse(final String response) {
        this.lichessMockResponse(HttpStatus.OK, response);
    }

    protected void lichessMockError(final HttpStatus status) {
        this.lichessMockResponse(status, "error");
    }

    private void lichessMockResponse(final HttpStatus status,
                                     final String response) {

        this.lichessMock.enqueue(new MockResponse()
            .addHeader(CONTENT_TYPE, status == HttpStatus.OK
                ? X_CHESS_PGN
                : TEXT_PLAIN)
            .setResponseCode(status.value())
            .setBody(response));
    }

}
