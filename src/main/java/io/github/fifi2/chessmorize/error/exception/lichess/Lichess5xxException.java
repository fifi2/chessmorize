package io.github.fifi2.chessmorize.error.exception.lichess;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class Lichess5xxException extends LichessException {

    public Lichess5xxException(final String message,
                               final HttpStatus status,
                               final Throwable cause) {

        super(message, status, cause);
    }

}
