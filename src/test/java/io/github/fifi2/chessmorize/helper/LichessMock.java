package io.github.fifi2.chessmorize.helper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public class LichessMock {

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String X_CHESS_PGN = "application/x-chess-pgn";
    private static final String TEXT_PLAIN = "text/plain";

    private MockWebServer lichessMock;

    public void start() throws IOException {

        if (this.lichessMock == null) {
            this.lichessMock = new MockWebServer();
            this.lichessMock.start(8081);
        }
    }

    public void stop() throws IOException {

        if (this.lichessMock != null) {
            this.lichessMock.shutdown();
            this.lichessMock = null;
        }
    }

    public void mockResponse(final String response) {

        mockResponse(HttpStatus.OK, response);
    }

    public void mockError(final HttpStatus status) {

        mockResponse(status, "error");
    }

    private void mockResponse(final HttpStatus status,
                              final String response) {

        this.lichessMock.enqueue(new MockResponse()
            .addHeader(
                CONTENT_TYPE,
                status == HttpStatus.OK ? X_CHESS_PGN : TEXT_PLAIN)
            .setResponseCode(status.value())
            .setBody(response));
    }

}
