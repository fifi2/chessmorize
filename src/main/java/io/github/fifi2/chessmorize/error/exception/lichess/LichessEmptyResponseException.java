package io.github.fifi2.chessmorize.error.exception.lichess;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class LichessEmptyResponseException extends LichessException {

    public LichessEmptyResponseException() {

        super("Unexpected empty response when calling Lichess", null, null);
    }

}
