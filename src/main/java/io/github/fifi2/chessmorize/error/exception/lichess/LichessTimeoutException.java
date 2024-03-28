package io.github.fifi2.chessmorize.error.exception.lichess;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class LichessTimeoutException extends LichessException {

    public LichessTimeoutException(final HttpStatus status,
                                   final Throwable cause) {

        super("Timeout during call to Lichess", status, cause);
    }

}
