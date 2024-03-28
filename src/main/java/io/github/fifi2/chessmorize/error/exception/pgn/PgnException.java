package io.github.fifi2.chessmorize.error.exception.pgn;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class PgnException extends RuntimeException {

    private final String message;
    private final Throwable cause;

    public PgnException(final String studyId, final Throwable cause) {
        this.message = "Exception during PGN parsing of study " + studyId;
        this.cause = cause;
    }

}
