package io.github.fifi2.chessmorize.error.exception.lichess;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LichessNotFoundException extends LichessException {

    public LichessNotFoundException(final String studyId,
                                    final Throwable cause) {

        super(
            "The study " + studyId + " is not found on Lichess.",
            HttpStatus.NOT_FOUND,
            cause);
    }

}
