package io.github.fifi2.chessmorize.error.exception.lichess;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class Lichess4xxException extends LichessException {

    public Lichess4xxException(final String message,
                               final HttpStatus status,
                               final Throwable cause) {

        super(message, status, cause);
    }

}
